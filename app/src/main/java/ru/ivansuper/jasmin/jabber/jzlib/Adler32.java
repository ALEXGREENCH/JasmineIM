package ru.ivansuper.jasmin.jabber.jzlib;

final class Adler32 {
    private static final int BASE = 65521;
    private static final int NMAX = 5552;

    Adler32() {
    }

    long adler32(long j, byte[] bArr, int i, int i2) {
        if (bArr == null) {
            return 1L;
        }
        int i3 = 16;
        long j2 = j & 65535;
        long j3 = 65535 & (j >> 16);
        int i4 = i;
        int i5 = i2;
        while (i5 > 0) {
            int i6 = NMAX;
            if (i5 < NMAX) {
                i6 = i5;
            }
            i5 -= i6;
            while (i6 >= i3) {
                int i7 = i4 + 1;
                long j4 = j2 + ((long) (bArr[i4] & 255));
                int i8 = i7 + 1;
                long j5 = ((long) (bArr[i7] & 255)) + j4;
                int i9 = i8 + 1;
                long j6 = ((long) (bArr[i8] & 255)) + j5;
                int i10 = i9 + 1;
                long j7 = ((long) (bArr[i9] & 255)) + j6;
                int i11 = i10 + 1;
                long j8 = ((long) (bArr[i10] & 255)) + j7;
                int i12 = i11 + 1;
                long j9 = ((long) (bArr[i11] & 255)) + j8;
                int i13 = i12 + 1;
                long j10 = ((long) (bArr[i12] & 255)) + j9;
                int i14 = i13 + 1;
                long j11 = ((long) (bArr[i13] & 255)) + j10;
                int i15 = i14 + 1;
                long j12 = ((long) (bArr[i14] & 255)) + j11;
                int i16 = i15 + 1;
                long j13 = ((long) (bArr[i15] & 255)) + j12;
                int i17 = i16 + 1;
                long j14 = ((long) (bArr[i16] & 255)) + j13;
                int i18 = i17 + 1;
                long j15 = ((long) (bArr[i17] & 255)) + j14;
                int i19 = i18 + 1;
                long j16 = ((long) (bArr[i18] & 255)) + j15;
                int i20 = i19 + 1;
                long j17 = ((long) (bArr[i19] & 255)) + j16;
                int i21 = i20 + 1;
                long j18 = ((long) (bArr[i20] & 255)) + j17;
                long j19 = ((long) (bArr[i21] & 255)) + j18;
                j3 = j3 + j4 + j5 + j6 + j7 + j8 + j9 + j10 + j11 + j12 + j13 + j14 + j15 + j16 + j17 + j18 + j19;
                i5 = i5;
                j2 = j19;
                i3 = 16;
                i6 -= 16;
                i4 = i21 + 1;
            }
            if (i6 != 0) {
                do {
                    j2 += (long) (bArr[i4] & 255);
                    j3 += j2;
                    i6--;
                    i4++;
                } while (i6 != 0);
            }
            j2 %= 65521;
            j3 %= 65521;
        }
        return (j3 << i3) | j2;
    }
}
