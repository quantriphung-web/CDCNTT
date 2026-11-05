import 'dart:convert';
import 'package:web_socket_channel/web_socket_channel.dart';

class WebSocketManager {
  static final WebSocketManager _instance = WebSocketManager._internal();
  factory WebSocketManager() => _instance;
  WebSocketManager._internal();

  final List<WebSocketChannel> _clients = [];

  void addClient(WebSocketChannel channel) {
    _clients.add(channel);
    print('[WebSocket] Client connected. Active clients: ${_clients.length}');

    // Send welcome ping
    try {
      channel.sink.add(jsonEncode({
        'type': 'CONNECTED',
        'message': 'Connected to Baby & Security Alarm Server',
        'timestamp': DateTime.now().toIso8601String(),
      }));
    } catch (_) {}

    channel.stream.listen(
      (data) {
        // Client can send ping / ack
        try {
          final decoded = jsonDecode(data.toString());
          if (decoded['type'] == 'PING') {
            channel.sink.add(jsonEncode({'type': 'PONG'}));
          }
        } catch (_) {}
      },
      onDone: () {
        _clients.remove(channel);
        print('[WebSocket] Client disconnected. Active clients: ${_clients.length}');
      },
      onError: (err) {
        _clients.remove(channel);
        print('[WebSocket] Client error: $err');
      },
    );
  }

  void broadcast(String eventType, Map<String, dynamic> payload) {
    final message = jsonEncode({
      'event': eventType,
      'data': payload,
      'timestamp': DateTime.now().toIso8601String(),
    });

    final deadClients = <WebSocketChannel>[];
    for (final client in _clients) {
      try {
        client.sink.add(message);
      } catch (e) {
        deadClients.add(client);
      }
    }
    _clients.removeWhere((c) => deadClients.contains(c));
    print('[WebSocket] Broadcasted $eventType to ${_clients.length} clients');
  }
}
