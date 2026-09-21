package ru.ivansuper.jasmin.security;

import java.util.Arrays;

/**
 * Argon2 (RFC 9106), version 0x13, in pure Java. Supports the d/i/id variants and any
 * lane count, but is used here only as Argon2id with a single lane. Memory is held as
 * one 1 KiB long[128] per block, so a 64 MiB run is 65536 small arrays instead of one
 * huge allocation the Android heap may refuse.
 */
public final class Argon2 {
    public static final int TYPE_D = 0;
    public static final int TYPE_I = 1;
    public static final int TYPE_ID = 2;

    private static final int VERSION = 0x13;
    private static final int BLOCK_WORDS = 128;   // 1024 bytes
    private static final int SYNC_POINTS = 4;
    private static final int ADDRESSES_IN_BLOCK = 128;

    private final int type;
    private final int lanes;
    private final int passes;
    private final int memoryKib;      // m as requested (hashed into H0)
    private final int memoryBlocks;   // m', rounded down to a multiple of 4*lanes
    private final int laneLength;     // q
    private final int segmentLength;
    private final long[][] memory;

    private Argon2(int type, int memoryKib, int iterations, int parallelism) {
        if (parallelism < 1 || iterations < 1 || memoryKib < 8 * parallelism) {
            throw new IllegalArgumentException("argon2 parameters");
        }
        this.type = type;
        this.lanes = parallelism;
        this.passes = iterations;
        this.memoryKib = memoryKib;
        this.memoryBlocks = (memoryKib / (SYNC_POINTS * parallelism)) * (SYNC_POINTS * parallelism);
        this.laneLength = memoryBlocks / parallelism;
        this.segmentLength = laneLength / SYNC_POINTS;
        this.memory = new long[memoryBlocks][];
    }

    /** Argon2 hash; {@code secret} and {@code ad} may be null. */
    public static byte[] hash(int type, byte[] password, byte[] salt, byte[] secret, byte[] ad,
                              int memoryKib, int iterations, int parallelism, int tagLength) {
        Argon2 a = new Argon2(type, memoryKib, iterations, parallelism);
        try {
            byte[] h0 = a.initialHash(password, salt, secret, ad, tagLength);
            a.fillFirstBlocks(h0);
            a.fillMemory();
            return a.finalize(tagLength);
        } finally {
            a.wipe();
        }
    }

    private byte[] initialHash(byte[] pw, byte[] salt, byte[] secret, byte[] ad, int tagLength) {
        Blake2b b = new Blake2b(64);
        b.updateIntLE(lanes);
        b.updateIntLE(tagLength);
        b.updateIntLE(memoryKib);
        b.updateIntLE(passes);
        b.updateIntLE(VERSION);
        b.updateIntLE(type);
        updateWithLength(b, pw);
        updateWithLength(b, salt);
        updateWithLength(b, secret);
        updateWithLength(b, ad);
        return b.digest();
    }

    private static void updateWithLength(Blake2b b, byte[] data) {
        if (data == null) {
            b.updateIntLE(0);
        } else {
            b.updateIntLE(data.length);
            b.update(data, 0, data.length);
        }
    }

    private void fillFirstBlocks(byte[] h0) {
        byte[] in = new byte[64 + 8];
        System.arraycopy(h0, 0, in, 0, 64);
        for (int lane = 0; lane < lanes; lane++) {
            for (int col = 0; col < 2; col++) {
                putIntLE(in, 64, col);
                putIntLE(in, 68, lane);
                memory[lane * laneLength + col] = toBlock(hashVariable(in, 1024));
            }
        }
    }

    /** H' from RFC 9106 §3.3: variable-length Blake2b. */
    static byte[] hashVariable(byte[] in, int outLen) {
        Blake2b first = new Blake2b(Math.min(outLen, 64));
        first.updateIntLE(outLen);
        first.update(in, 0, in.length);
        if (outLen <= 64) {
            return first.digest();
        }
        byte[] out = new byte[outLen];
        int r = (outLen + 31) / 32 - 2;
        byte[] v = first.digest();
        int pos = 0;
        for (int i = 0; i < r; i++) {
            System.arraycopy(v, 0, out, pos, 32);
            pos += 32;
            int nextLen = (i == r - 1) ? outLen - 32 * r : 64;
            v = Blake2b.hash(nextLen, v);
        }
        System.arraycopy(v, 0, out, pos, v.length);
        return out;
    }

    private void fillMemory() {
        for (int pass = 0; pass < passes; pass++) {
            for (int slice = 0; slice < SYNC_POINTS; slice++) {
                for (int lane = 0; lane < lanes; lane++) {
                    fillSegment(pass, lane, slice);
                }
            }
        }
    }

    private void fillSegment(int pass, int lane, int slice) {
        boolean dataIndependent = type == TYPE_I || (type == TYPE_ID && pass == 0 && slice < SYNC_POINTS / 2);
        long[] inputBlock = null, addressBlock = null, zeroBlock = null;
        if (dataIndependent) {
            inputBlock = new long[BLOCK_WORDS];
            addressBlock = new long[BLOCK_WORDS];
            zeroBlock = new long[BLOCK_WORDS];
            inputBlock[0] = pass;
            inputBlock[1] = lane;
            inputBlock[2] = slice;
            inputBlock[3] = memoryBlocks;
            inputBlock[4] = passes;
            inputBlock[5] = type;
        }
        int startingIndex = 0;
        if (pass == 0 && slice == 0) {
            startingIndex = 2;   // the first two blocks of each lane come from H0
            if (dataIndependent) nextAddresses(addressBlock, inputBlock, zeroBlock);
        }
        int curIndex = lane * laneLength + slice * segmentLength + startingIndex;
        int prevIndex = (curIndex % laneLength == 0) ? curIndex + laneLength - 1 : curIndex - 1;

        for (int i = startingIndex; i < segmentLength; i++, curIndex++, prevIndex++) {
            if (curIndex % laneLength == 1) prevIndex = curIndex - 1;
            long pseudoRand;
            if (dataIndependent) {
                if (i % ADDRESSES_IN_BLOCK == 0) nextAddresses(addressBlock, inputBlock, zeroBlock);
                pseudoRand = addressBlock[i % ADDRESSES_IN_BLOCK];
            } else {
                pseudoRand = memory[prevIndex][0];
            }
            int refLane = (int) ((pseudoRand >>> 32) % lanes);
            if (pass == 0 && slice == 0) refLane = lane;
            int refIndex = indexAlpha(pass, slice, i, (int) pseudoRand, refLane == lane);
            long[] ref = memory[refLane * laneLength + refIndex];
            long[] cur = memory[curIndex];
            if (cur == null) {
                cur = new long[BLOCK_WORDS];
                memory[curIndex] = cur;
                fillBlock(memory[prevIndex], ref, cur, false);
            } else {
                fillBlock(memory[prevIndex], ref, cur, true);
            }
        }
    }

    private void nextAddresses(long[] address, long[] input, long[] zero) {
        input[6]++;
        fillBlock(zero, input, address, false);
        fillBlock(zero, address, address, false);
    }

    private int indexAlpha(int pass, int slice, int index, int pseudoRand, boolean sameLane) {
        long referenceAreaSize;
        if (pass == 0) {
            if (slice == 0) {
                referenceAreaSize = index - 1;
            } else if (sameLane) {
                referenceAreaSize = (long) slice * segmentLength + index - 1;
            } else {
                referenceAreaSize = (long) slice * segmentLength + (index == 0 ? -1 : 0);
            }
        } else if (sameLane) {
            referenceAreaSize = (long) laneLength - segmentLength + index - 1;
        } else {
            referenceAreaSize = (long) laneLength - segmentLength + (index == 0 ? -1 : 0);
        }
        long rel = pseudoRand & 0xFFFFFFFFL;
        rel = (rel * rel) >>> 32;
        rel = referenceAreaSize - 1 - ((referenceAreaSize * rel) >>> 32);
        long start = 0;
        if (pass != 0) start = (slice == SYNC_POINTS - 1) ? 0 : (long) (slice + 1) * segmentLength;
        return (int) ((start + rel) % laneLength);
    }

    // Scratch blocks reused by every fillBlock call (one instance runs on one thread).
    private final long[] scratchR = new long[BLOCK_WORDS];
    private final long[] scratchW = new long[BLOCK_WORDS];

    /** next = (withXor ? next : 0) ^ R ^ P(R), where R = prev ^ ref. {@code next} may alias {@code ref}. */
    private void fillBlock(long[] prev, long[] ref, long[] next, boolean withXor) {
        long[] r = scratchR;
        long[] w = scratchW;
        for (int i = 0; i < BLOCK_WORDS; i++) {
            r[i] = prev[i] ^ ref[i];
            w[i] = r[i];
        }
        // rows: 8 permutations over 16 consecutive words
        for (int i = 0; i < 8; i++) {
            int b = i * 16;
            permute(w, b, b + 1, b + 2, b + 3, b + 4, b + 5, b + 6, b + 7,
                    b + 8, b + 9, b + 10, b + 11, b + 12, b + 13, b + 14, b + 15);
        }
        // columns: 8 permutations over the word pairs of each column
        for (int i = 0; i < 8; i++) {
            int b = i * 2;
            permute(w, b, b + 1, b + 16, b + 17, b + 32, b + 33, b + 48, b + 49,
                    b + 64, b + 65, b + 80, b + 81, b + 96, b + 97, b + 112, b + 113);
        }
        if (withXor) {
            for (int i = 0; i < BLOCK_WORDS; i++) next[i] ^= r[i] ^ w[i];
        } else {
            for (int i = 0; i < BLOCK_WORDS; i++) next[i] = r[i] ^ w[i];
        }
    }

    private static void permute(long[] v, int v0, int v1, int v2, int v3, int v4, int v5, int v6, int v7,
                                int v8, int v9, int v10, int v11, int v12, int v13, int v14, int v15) {
        g(v, v0, v4, v8, v12);
        g(v, v1, v5, v9, v13);
        g(v, v2, v6, v10, v14);
        g(v, v3, v7, v11, v15);
        g(v, v0, v5, v10, v15);
        g(v, v1, v6, v11, v12);
        g(v, v2, v7, v8, v13);
        g(v, v3, v4, v9, v14);
    }

    private static void g(long[] v, int a, int b, int c, int d) {
        v[a] = v[a] + v[b] + 2 * (v[a] & 0xFFFFFFFFL) * (v[b] & 0xFFFFFFFFL);
        v[d] = Long.rotateRight(v[d] ^ v[a], 32);
        v[c] = v[c] + v[d] + 2 * (v[c] & 0xFFFFFFFFL) * (v[d] & 0xFFFFFFFFL);
        v[b] = Long.rotateRight(v[b] ^ v[c], 24);
        v[a] = v[a] + v[b] + 2 * (v[a] & 0xFFFFFFFFL) * (v[b] & 0xFFFFFFFFL);
        v[d] = Long.rotateRight(v[d] ^ v[a], 16);
        v[c] = v[c] + v[d] + 2 * (v[c] & 0xFFFFFFFFL) * (v[d] & 0xFFFFFFFFL);
        v[b] = Long.rotateRight(v[b] ^ v[c], 63);
    }

    private byte[] finalize(int tagLength) {
        long[] acc = new long[BLOCK_WORDS];
        for (int lane = 0; lane < lanes; lane++) {
            long[] last = memory[lane * laneLength + laneLength - 1];
            for (int i = 0; i < BLOCK_WORDS; i++) acc[i] ^= last[i];
        }
        return hashVariable(fromBlock(acc), tagLength);
    }

    private void wipe() {
        for (long[] b : memory) if (b != null) Arrays.fill(b, 0L);
    }

    private static long[] toBlock(byte[] bytes) {
        long[] b = new long[BLOCK_WORDS];
        for (int i = 0; i < BLOCK_WORDS; i++) {
            int p = i * 8;
            b[i] = (bytes[p] & 0xFFL) | ((bytes[p + 1] & 0xFFL) << 8) | ((bytes[p + 2] & 0xFFL) << 16)
                    | ((bytes[p + 3] & 0xFFL) << 24) | ((bytes[p + 4] & 0xFFL) << 32) | ((bytes[p + 5] & 0xFFL) << 40)
                    | ((bytes[p + 6] & 0xFFL) << 48) | ((bytes[p + 7] & 0xFFL) << 56);
        }
        return b;
    }

    private static byte[] fromBlock(long[] b) {
        byte[] out = new byte[BLOCK_WORDS * 8];
        for (int i = 0; i < BLOCK_WORDS; i++) {
            long x = b[i];
            for (int j = 0; j < 8; j++) out[i * 8 + j] = (byte) (x >>> (8 * j));
        }
        return out;
    }

    private static void putIntLE(byte[] b, int off, int x) {
        b[off] = (byte) x;
        b[off + 1] = (byte) (x >>> 8);
        b[off + 2] = (byte) (x >>> 16);
        b[off + 3] = (byte) (x >>> 24);
    }
}
