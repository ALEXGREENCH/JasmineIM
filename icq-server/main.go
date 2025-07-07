package main

import (
	"bufio"
	"encoding/binary"
	"fmt"
	"io"
	"log"
	"net"
	"strings"
	"sync"
)

type Client struct {
	conn     net.Conn
	id       int
	seq      uint16
	name     string
	contacts map[string]bool
}

type Server struct {
	mu           sync.Mutex
	clients      map[int]*Client
	accounts     map[string]string
	contactLists map[string]map[string]bool
	statuses     map[string]string
	xstatuses    map[string]string
	nextID       int
}

// TLV represents a simple Type-Length-Value tuple used in OSCAR.
type TLV struct {
	Type  uint16
	Value []byte
}

func parseTLVs(data []byte) ([]TLV, error) {
	res := []TLV{}
	for len(data) >= 4 {
		t := binary.BigEndian.Uint16(data[:2])
		l := binary.BigEndian.Uint16(data[2:4])
		if int(l)+4 > len(data) {
			return res, io.ErrUnexpectedEOF
		}
		v := make([]byte, l)
		copy(v, data[4:4+int(l)])
		res = append(res, TLV{Type: t, Value: v})
		data = data[4+int(l):]
	}
	if len(data) != 0 {
		return res, io.ErrUnexpectedEOF
	}
	return res, nil
}

// unroastPassword decodes the legacy ICQ "roasted" password format used in
// CLI_IDENT packets. Each byte of the encoded password is XORed with a repeating
// sequence defined by the OSCAR documentation.
func unroastPassword(enc []byte) string {
	key := []byte{0xF3, 0x26, 0x81, 0xC4, 0x39, 0x86, 0xDB, 0x92, 0x71,
		0xA3, 0xB9, 0xE6, 0x53, 0x7A, 0x95, 0x7C}
	out := make([]byte, len(enc))
	for i, b := range enc {
		out[i] = b ^ key[i%len(key)]
	}
	return string(out)
}

func buildTLV(t uint16, v []byte) []byte {
	buf := make([]byte, 4+len(v))
	binary.BigEndian.PutUint16(buf[:2], t)
	binary.BigEndian.PutUint16(buf[2:4], uint16(len(v)))
	copy(buf[4:], v)
	return buf
}

func NewServer() *Server {
	return &Server{
		clients:      make(map[int]*Client),
		accounts:     make(map[string]string),
		contactLists: make(map[string]map[string]bool),
		statuses:     make(map[string]string),
		xstatuses:    make(map[string]string),
	}
}

func (s *Server) broadcast(sender *Client, msg string) {
	s.mu.Lock()
	defer s.mu.Unlock()
	for id, c := range s.clients {
		if id == sender.id {
			continue
		}
		sendFLAP(c.conn, 2, c.seq, []byte(fmt.Sprintf("MSG from %s: %s", sender.name, msg)))
		c.seq++
	}
}

func (s *Server) sendContactList(to *Client) {
	s.mu.Lock()
	names := make([]string, 0, len(to.contacts))
	for name := range to.contacts {
		names = append(names, name)
	}
	s.mu.Unlock()
	payload := []byte("LIST:" + strings.Join(names, ","))
	sendFLAP(to.conn, 3, to.seq, payload)
	to.seq++
}

func (s *Server) sendStatuses(to *Client) {
	s.mu.Lock()
	for name, status := range s.statuses {
		payload := []byte("STATUS:" + name + ":" + status)
		sendFLAP(to.conn, 3, to.seq, payload)
		to.seq++
	}
	for name, status := range s.xstatuses {
		payload := []byte("XSTATUS:" + name + ":" + status)
		sendFLAP(to.conn, 3, to.seq, payload)
		to.seq++
	}
	s.mu.Unlock()
}

func (s *Server) broadcastPresence(name, action string, exclude *Client) {
	s.mu.Lock()
	for _, c := range s.clients {
		if exclude != nil && c.id == exclude.id {
			continue
		}
		payload := []byte(action + ":" + name)
		sendFLAP(c.conn, 3, c.seq, payload)
		c.seq++
	}
	s.mu.Unlock()
}

func (s *Server) broadcastStatus(name, status string) {
	s.mu.Lock()
	for _, c := range s.clients {
		payload := []byte("STATUS:" + name + ":" + status)
		sendFLAP(c.conn, 3, c.seq, payload)
		c.seq++
	}
	s.mu.Unlock()
}

func (s *Server) broadcastXStatus(name, status string) {
	s.mu.Lock()
	for _, c := range s.clients {
		payload := []byte("XSTATUS:" + name + ":" + status)
		sendFLAP(c.conn, 3, c.seq, payload)
		c.seq++
	}
	s.mu.Unlock()
}

func (s *Server) handleCommand(c *Client, cmd string) {
	if strings.HasPrefix(cmd, "SEARCH:") {
		q := strings.TrimPrefix(cmd, "SEARCH:")
		s.mu.Lock()
		names := []string{}
		for name := range s.accounts {
			if strings.Contains(name, q) {
				names = append(names, name)
			}
		}
		s.mu.Unlock()
		payload := []byte("RESULTS:" + strings.Join(names, ","))
		sendFLAP(c.conn, 3, c.seq, payload)
		c.seq++
	} else if strings.HasPrefix(cmd, "ADD_CONTACT:") {
		name := strings.TrimPrefix(cmd, "ADD_CONTACT:")
		s.mu.Lock()
		if _, ok := s.contactLists[c.name]; !ok {
			s.contactLists[c.name] = make(map[string]bool)
		}
		s.contactLists[c.name][name] = true
		c.contacts = s.contactLists[c.name]
		s.mu.Unlock()
		s.sendContactList(c)
	} else if strings.HasPrefix(cmd, "REMOVE_CONTACT:") {
		name := strings.TrimPrefix(cmd, "REMOVE_CONTACT:")
		s.mu.Lock()
		if list, ok := s.contactLists[c.name]; ok {
			delete(list, name)
		}
		c.contacts = s.contactLists[c.name]
		s.mu.Unlock()
		s.sendContactList(c)
	} else if strings.HasPrefix(cmd, "STATUS:") {
		status := strings.TrimPrefix(cmd, "STATUS:")
		s.mu.Lock()
		s.statuses[c.name] = status
		s.mu.Unlock()
		s.broadcastStatus(c.name, status)
	} else if strings.HasPrefix(cmd, "XSTATUS:") {
		status := strings.TrimPrefix(cmd, "XSTATUS:")
		s.mu.Lock()
		s.xstatuses[c.name] = status
		s.mu.Unlock()
		s.broadcastXStatus(c.name, status)
	}
}

func sendFLAP(conn net.Conn, channel byte, seq uint16, data []byte) {
	header := make([]byte, 6)
	header[0] = '*'
	header[1] = channel
	binary.BigEndian.PutUint16(header[2:], seq)
	binary.BigEndian.PutUint16(header[4:], uint16(len(data)))
	conn.Write(header)
	conn.Write(data)
}

func sendCookie(conn net.Conn, seq uint16, name string) {
	bos := []byte("localhost:5190")
	cookie := []byte("dummycookie")
	payload := append(buildTLV(0x01, []byte(name)), buildTLV(0x05, bos)...)
	payload = append(payload, buildTLV(0x06, cookie)...)
	sendFLAP(conn, 4, seq, payload)
}

func (s *Server) handleConn(conn net.Conn) {
	defer conn.Close()
	s.mu.Lock()
	id := s.nextID
	s.nextID++
	client := &Client{conn: conn, id: id}
	s.clients[id] = client
	s.mu.Unlock()

	defer func() {
		s.mu.Lock()
		delete(s.clients, id)
		s.mu.Unlock()
		if client.name != "" {
			s.broadcastPresence(client.name, "REMOVE", nil)
		}
	}()

	reader := bufio.NewReader(conn)

	// FLAP version handshake (4 bytes 0x00000001)
	ver := make([]byte, 4)
	if _, err := io.ReadFull(reader, ver); err != nil {
		log.Println("version handshake:", err)
		return
	}
	if binary.BigEndian.Uint32(ver) != 1 {
		log.Println("unsupported version")
		return
	}
	// echo version back
	conn.Write(ver)

	for {
		header := make([]byte, 6)
		if _, err := reader.Read(header); err != nil {
			log.Println("read header:", err)
			return
		}
		if header[0] != '*' {
			log.Println("invalid packet")
			return
		}
		channel := header[1]
		seq := binary.BigEndian.Uint16(header[2:4])
		length := binary.BigEndian.Uint16(header[4:6])
		data := make([]byte, length)
		if _, err := reader.Read(data); err != nil {
			log.Println("read data:", err)
			return
		}
		if channel == 1 {
			tlvs, err := parseTLVs(data)
			if err != nil {
				log.Println("parse tlv:", err)
				return
			}
			var name, pass string
			for _, tlv := range tlvs {
				switch tlv.Type {
				case 0x01:
					name = strings.TrimSpace(string(tlv.Value))
				case 0x02:
					pass = unroastPassword(tlv.Value)
				}
			}
			if name == "" {
				log.Println("empty login")
				return
			}
			s.mu.Lock()
			p, ok := s.accounts[name]
			if ok {
				if p != pass {
					s.mu.Unlock()
					log.Println("bad password for", name)
					return
				}
			} else {
				s.accounts[name] = pass
				s.contactLists[name] = make(map[string]bool)
			}
			client.name = name
			client.contacts = s.contactLists[name]
			s.mu.Unlock()
			sendCookie(conn, seq, client.name)
			s.sendContactList(client)
			s.sendStatuses(client)
			s.broadcastPresence(client.name, "ADD", client)
		} else if channel == 2 {
			msg := string(data)
			s.broadcast(client, msg)
		} else if channel == 3 {
			s.handleCommand(client, string(data))
		}
	}
}

func (s *Server) Listen(addr string) error {
	ln, err := net.Listen("tcp", addr)
	if err != nil {
		return err
	}
	log.Println("ICQ test server listening on", addr)
	for {
		conn, err := ln.Accept()
		if err != nil {
			log.Println("accept:", err)
			continue
		}
		go s.handleConn(conn)
	}
}

func main() {
	addr := ":5190"
	srv := NewServer()
	if err := srv.Listen(addr); err != nil {
		log.Fatal(err)
	}
}
