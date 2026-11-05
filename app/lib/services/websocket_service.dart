import 'dart:async';
import 'dart:convert';
import 'package:web_socket_channel/web_socket_channel.dart';
import '../models/alert_item.dart';


class WebSocketService {
  WebSocketChannel? _channel;
  Timer? _reconnectTimer;
  bool _isConnected = false;

  Function(AlertItem)? onNewAlert;
  Function(Map<String, dynamic>)? onStatusUpdate;
  Function(int)? onAlertResolved;
  Function(int)? onAlertDeleted;
  Function()? onAlertsCleared;
  Function(bool)? onConnectionChange;

  bool get isConnected => _isConnected;

  void connect(String wsUrl) {
    _disconnect();
    try {
      final uri = Uri.parse(wsUrl);
      _channel = WebSocketChannel.connect(uri);
      _isConnected = true;
      onConnectionChange?.call(true);
      print('[WS Service] Connected to $wsUrl');

      _channel!.stream.listen(
        (message) {
          _handleMessage(message.toString());
        },
        onDone: () {
          print('[WS Service] Connection closed');
          _isConnected = false;
          onConnectionChange?.call(false);
          _scheduleReconnect(wsUrl);
        },
        onError: (err) {
          print('[WS Service] Error: $err');
          _isConnected = false;
          onConnectionChange?.call(false);
          _scheduleReconnect(wsUrl);
        },
      );
    } catch (e) {
      print('[WS Service] Connect exception: $e');
      _isConnected = false;
      onConnectionChange?.call(false);
      _scheduleReconnect(wsUrl);
    }
  }

  void _handleMessage(String raw) {
    try {
      final json = jsonDecode(raw) as Map<String, dynamic>;
      final event = json['event'];
      final data = json['data'];

      if (event == 'NEW_ALERT' && data is Map<String, dynamic>) {
        final alert = AlertItem.fromJson(data);
        onNewAlert?.call(alert);
      } else if (event == 'STATUS_UPDATE' && data is Map<String, dynamic>) {
        onStatusUpdate?.call(data);
      } else if (event == 'ALERT_RESOLVED' && data is Map<String, dynamic>) {
        final id = data['id'] as int? ?? 0;
        onAlertResolved?.call(id);
      } else if (event == 'ALERT_DELETED' && data is Map<String, dynamic>) {
        final id = data['id'] as int? ?? 0;
        onAlertDeleted?.call(id);
      } else if (event == 'ALERTS_CLEARED') {
        onAlertsCleared?.call();
      }
    } catch (e) {
      print('[WS Service] Handle message error: $e');
    }
  }

  void _scheduleReconnect(String wsUrl) {
    _reconnectTimer?.cancel();
    _reconnectTimer = Timer(const Duration(seconds: 4), () {
      print('[WS Service] Attempting reconnect...');
      connect(wsUrl);
    });
  }

  void _disconnect() {
    _reconnectTimer?.cancel();
    try {
      _channel?.sink.close();
    } catch (_) {}
    _channel = null;
    _isConnected = false;
  }

  void dispose() {
    _disconnect();
  }
}
