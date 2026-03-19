import SockJS from "sockjs-client";
import { Client } from "@stomp/stompjs";

let stompClient: Client | null = null;

/**
 * Connect to WebSocket and subscribe to /topic/parking
 * Calls onMessage(data) when a valid JSON message is received
 */
export const connectWebSocket = (onMessage: (data: any) => void) => {
  const socket = new SockJS("/ws"); // Proxy forwards to backend /ws
  stompClient = new Client({
    webSocketFactory: () => socket,
    debug: (str) => console.log("STOMP:", str),
    onConnect: () => {
      console.log("WebSocket connected");
      stompClient!.subscribe("/topic/parking", (msg) => {
        try {
          const data = JSON.parse(msg.body);
          onMessage(data);
        } catch (err) {
          console.error("WebSocket: Failed to parse JSON message:", msg.body);
        }
      });
    },
    onStompError: (frame) => {
      console.error("WebSocket STOMP error:", frame);
    },
    onWebSocketError: (evt) => {
      console.error("WebSocket connection error:", evt);
    },
  });

  stompClient.activate();
};

/**
 * Disconnect from WebSocket
 */
export const disconnectWebSocket = () => {
  if (stompClient) {
    stompClient.deactivate();
    stompClient = null;
    console.log("WebSocket disconnected");
  }
};