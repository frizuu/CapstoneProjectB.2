package handler

import (
	"log"
	"net/http"
	"strconv"
	"sync"

	"github.com/gorilla/websocket"
)

// 1. The Standardized Message Format
type WSMessage struct {
	Type    string      `json:"type"`    // e.g., "TRANSACTION_SUCCESS", "ERROR"
	Payload interface{} `json:"payload"` // The actual data
}

// 2. The Connection Manager
type WSManager struct {
	// Maps a User ID to their active WebSocket connection
	clients map[int]*websocket.Conn
	// Mutex to prevent concurrent map writes and crashes
	mu sync.RWMutex
}

// Create a new instance of the manager
func NewWSManager() *WSManager {
	return &WSManager{
		clients: make(map[int]*websocket.Conn),
	}
}

var upgrader = websocket.Upgrader{
	CheckOrigin: func(r *http.Request) bool { return true },
}

// 3. The Upgraded Handler
func (m *WSManager) HandleWebSocket(w http.ResponseWriter, r *http.Request) {
	conn, err := upgrader.Upgrade(w, r, nil)
	if err != nil {
		log.Println("Upgrade Error:", err)
		return
	}

	userID, err := strconv.Atoi(r.URL.Query().Get("user_id"))
	if err != nil || userID <= 0 {
		log.Println("WebSocket rejected: user_id query parameter is required")
		conn.Close()
		return
	}

	// Safely add the connection to our map
	m.mu.Lock()
	m.clients[userID] = conn
	m.mu.Unlock()

	log.Printf("User %d connected to WebSocket\n", userID)

	// Clean up the connection when the user disconnects
	defer func() {
		m.mu.Lock()
		delete(m.clients, userID)
		m.mu.Unlock()
		conn.Close()
		log.Printf("User %d disconnected\n", userID)
	}()

	// Keep the connection alive
	for {
		if _, _, err := conn.ReadMessage(); err != nil {
			break
		}
	}
}

// 4. The Magic Method: Send data from ANYWHERE in your backend
func (m *WSManager) SendToUser(userID int, messageType string, data interface{}) {
	m.mu.RLock()
	conn, exists := m.clients[userID]
	m.mu.RUnlock()

	if !exists {
		// User is not currently connected to the app, do nothing or send a push notification
		return
	}

	msg := WSMessage{
		Type:    messageType,
		Payload: data,
	}

	// Safely encode and write the message
	m.mu.Lock()
	err := conn.WriteJSON(msg)
	m.mu.Unlock()

	if err != nil {
		log.Printf("Error sending message to user %d: %v\n", userID, err)
	}
}
