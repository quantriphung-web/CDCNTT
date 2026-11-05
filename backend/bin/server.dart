import 'dart:convert';
import 'dart:io';

import 'package:shelf/shelf.dart';
import 'package:shelf/shelf_io.dart' as io;
import 'package:shelf_cors_headers/shelf_cors_headers.dart';
import 'package:shelf_router/shelf_router.dart';
import 'package:shelf_web_socket/shelf_web_socket.dart';

import '../lib/database.dart';
import '../lib/models/alert.dart';
import '../lib/websocket_manager.dart';

void main(List<String> args) async {
  final ip = InternetAddress.anyIPv4;
  final port = int.tryParse(Platform.environment['PORT'] ?? '8080') ?? 8080;

  final db = DatabaseService();
  try {
    await db.initDatabase();
  } catch (e) {
    print('[Database] Warning: Could not connect to PostgreSQL immediately: $e');
  }

  final wsManager = WebSocketManager();

  // Router configuration
  final router = Router();

  // Static web directory for Flutter Web PWA
  final webDir = Directory(
    Platform.environment['WEB_DIR'] ??
        Platform.script.resolve('../../app/build/web').toFilePath(),
  );

  Response serveStatic(Request request) {
    if (!webDir.existsSync()) {
      return Response.ok(
        jsonEncode({
          'service': 'Baby & Anti-Theft AI Security Backend',
          'status': 'ONLINE',
          'version': '1.0.0',
          'timestamp': DateTime.now().toIso8601String(),
        }),
        headers: {'content-type': 'application/json'},
      );
    }
    String path = request.url.path;
    if (path.isEmpty || path == '/') {
      path = 'index.html';
    }
    // Normalize path for Windows
    final normalizedPath = path.replaceAll('/', Platform.pathSeparator);
    final file = File('${webDir.path}${Platform.pathSeparator}$normalizedPath');
    if (file.existsSync() && !FileSystemEntity.isDirectorySync(file.path)) {
      final ext = path.split('.').last.toLowerCase();
      final mimeTypes = {
        'html': 'text/html; charset=utf-8',
        'js': 'application/javascript; charset=utf-8',
        'json': 'application/json; charset=utf-8',
        'png': 'image/png',
        'jpg': 'image/jpeg',
        'jpeg': 'image/jpeg',
        'wasm': 'application/wasm',
        'css': 'text/css; charset=utf-8',
        'svg': 'image/svg+xml',
        'otf': 'font/otf',
        'ttf': 'font/ttf',
        'apk': 'application/vnd.android.package-archive',
      };
      return Response.ok(
        file.readAsBytesSync(),
        headers: {
          'content-type': mimeTypes[ext] ?? 'application/octet-stream',
          if (ext == 'apk') 'content-disposition': 'attachment; filename="baby_security_app.apk"',
        },
      );
    }
    // Fallback to index.html for Flutter Web SPA
    final indexFile = File('${webDir.path}${Platform.pathSeparator}index.html');
    if (indexFile.existsSync()) {
      return Response.ok(
        indexFile.readAsBytesSync(),
        headers: {'content-type': 'text/html; charset=utf-8'},
      );
    }
    return Response.notFound('Not found');
  }

  // Health check
  router.get('/api/health', (Request request) {
    return Response.ok(
      jsonEncode({'status': 'ok', 'timestamp': DateTime.now().toIso8601String()}),
      headers: {'content-type': 'application/json'},
    );
  });

  // WebSocket endpoint
  final wsHandler = webSocketHandler((channel, pingInterval) {
    wsManager.addClient(channel);
  });
  router.get('/ws', wsHandler);

  // GET /api/alerts
  router.get('/api/alerts', (Request request) async {
    try {
      final queryParams = request.url.queryParameters;
      final limit = int.tryParse(queryParams['limit'] ?? '50') ?? 50;
      final offset = int.tryParse(queryParams['offset'] ?? '0') ?? 0;
      final type = queryParams['type'];
      final unreadOnly = queryParams['unread_only'] == 'true';

      final alerts = await db.getAlerts(
        limit: limit,
        offset: offset,
        type: type,
        unreadOnly: unreadOnly,
      );

      return Response.ok(
        jsonEncode({
          'success': true,
          'count': alerts.length,
          'alerts': alerts.map((a) => a.toJson()).toList(),
        }),
        headers: {'content-type': 'application/json'},
      );
    } catch (e) {
      return Response.internalServerError(
        body: jsonEncode({'success': false, 'error': e.toString()}),
        headers: {'content-type': 'application/json'},
      );
    }
  });

  // POST /api/alerts (Called by Python AI demo or test client)
  router.post('/api/alerts', (Request request) async {
    try {
      final payload = await request.readAsString();
      final data = jsonDecode(payload) as Map<String, dynamic>;

      final alertType = (data['alert_type'] ?? 'unknown').toString();
      final title = (data['title'] ?? 'Cảnh báo hệ thống').toString();
      final message = (data['message'] ?? '').toString();
      final severity = (data['severity'] ?? 'INFO').toString().toUpperCase();
      final imageBase64 = data['image_base64'] as String?;
      final imagePath = data['image_path'] as String?;
      final trackId = data['track_id'] as int?;

      final alert = await db.createAlert(
        alertType: alertType,
        title: title,
        message: message,
        severity: severity,
        imageBase64: imageBase64,
        imagePath: imagePath,
        trackId: trackId,
      );

      // Update system status based on alert
      if (alertType == 'intruder' || alertType == 'stranger') {
        await db.updateSystemStatus(
          securityStatus: 'BAO DONG: PHAT HIEN NGUOI LA!',
          securityStatusColor: 'RED',
        );
      } else if (alertType == 'baby_danger' || alertType == 'fall') {
        await db.updateSystemStatus(
          babyStatus: 'NGUY CO ROI KHOI GIUONG!',
          babyStatusColor: 'RED',
        );
      } else if (alertType == 'baby_wake' || alertType == 'wake') {
        await db.updateSystemStatus(
          babyStatus: 'TRE DANG THUC',
          babyStatusColor: 'GREEN',
        );
      } else if (alertType == 'baby_sleep' || alertType == 'sleep') {
        await db.updateSystemStatus(
          babyStatus: 'TRE DANG NGU (zZz)',
          babyStatusColor: 'BLUE',
        );
      }

      // Broadcast real-time event to Flutter mobile apps
      wsManager.broadcast('NEW_ALERT', alert.toJson());

      final latestStatus = await db.getSystemStatus();
      wsManager.broadcast('STATUS_UPDATE', latestStatus);

      return Response.ok(
        jsonEncode({'success': true, 'alert': alert.toJson()}),
        headers: {'content-type': 'application/json'},
      );
    } catch (e) {
      print('[Server Error] POST /api/alerts: $e');
      return Response.internalServerError(
        body: jsonEncode({'success': false, 'error': e.toString()}),
        headers: {'content-type': 'application/json'},
      );
    }
  });

  // POST /api/alerts/<id>/read
  router.post('/api/alerts/<id>/read', (Request request, String id) async {
    final alertId = int.tryParse(id);
    if (alertId == null) {
      return Response.badRequest(body: 'Invalid alert ID');
    }
    final success = await db.markAsRead(alertId);
    return Response.ok(
      jsonEncode({'success': success, 'id': alertId}),
      headers: {'content-type': 'application/json'},
    );
  });

  // POST /api/alerts/<id>/resolve
  router.post('/api/alerts/<id>/resolve', (Request request, String id) async {
    final alertId = int.tryParse(id);
    if (alertId == null) {
      return Response.badRequest(body: 'Invalid alert ID');
    }
    final success = await db.markAsResolved(alertId);

    // Reset status to safe if resolved
    await db.updateSystemStatus(
      securityStatus: 'AN TOAN (Da xu ly)',
      securityStatusColor: 'GREEN',
    );

    wsManager.broadcast('ALERT_RESOLVED', {'id': alertId});
    final status = await db.getSystemStatus();
    wsManager.broadcast('STATUS_UPDATE', status);

    return Response.ok(
      jsonEncode({'success': success, 'id': alertId}),
      headers: {'content-type': 'application/json'},
    );
  });

  // DELETE /api/alerts/<id>
  router.delete('/api/alerts/<id>', (Request request, String id) async {
    final alertId = int.tryParse(id);
    if (alertId == null) {
      return Response.badRequest(body: 'Invalid alert ID');
    }
    final success = await db.deleteAlert(alertId);
    if (success) {
      wsManager.broadcast('ALERT_DELETED', {'id': alertId});
    }
    return Response.ok(
      jsonEncode({'success': success, 'id': alertId}),
      headers: {'content-type': 'application/json'},
    );
  });

  // DELETE /api/alerts (Clear all)
  router.delete('/api/alerts', (Request request) async {
    final success = await db.clearAllAlerts();
    await db.updateSystemStatus(
      securityStatus: 'AN TOAN',
      securityStatusColor: 'GREEN',
      babyStatus: 'HOAT DONG BINH THUONG',
      babyStatusColor: 'GREEN',
    );
    wsManager.broadcast('ALERTS_CLEARED', {});
    final status = await db.getSystemStatus();
    wsManager.broadcast('STATUS_UPDATE', status);

    return Response.ok(
      jsonEncode({'success': success}),
      headers: {'content-type': 'application/json'},
    );
  });

  // GET /api/status
  router.get('/api/status', (Request request) async {
    try {
      final status = await db.getSystemStatus();
      return Response.ok(
        jsonEncode({'success': true, 'status': status}),
        headers: {'content-type': 'application/json'},
      );
    } catch (e) {
      return Response.internalServerError(
        body: jsonEncode({'success': false, 'error': e.toString()}),
        headers: {'content-type': 'application/json'},
      );
    }
  });

  // POST /api/status (Heartbeat and state update from Python)
  router.post('/api/status', (Request request) async {
    try {
      final payload = await request.readAsString();
      final data = jsonDecode(payload) as Map<String, dynamic>;

      await db.updateSystemStatus(
        babyStatus: data['baby_status'] as String?,
        babyStatusColor: data['baby_status_color'] as String?,
        securityStatus: data['security_status'] as String?,
        securityStatusColor: data['security_status_color'] as String?,
        isCameraOnline: data['is_camera_online'] as bool?,
        activeMode: data['active_mode'] as String?,
        lastSnapshotUrl: data['last_snapshot_url'] as String?,
      );

      final latest = await db.getSystemStatus();
      wsManager.broadcast('STATUS_UPDATE', latest);

      return Response.ok(
        jsonEncode({'success': true, 'status': latest}),
        headers: {'content-type': 'application/json'},
      );
    } catch (e) {
      return Response.internalServerError(
        body: jsonEncode({'success': false, 'error': e.toString()}),
        headers: {'content-type': 'application/json'},
      );
    }
  });

  // GET /api/statistics
  router.get('/api/statistics', (Request request) async {
    try {
      final stats = await db.getStatistics();
      return Response.ok(
        jsonEncode({'success': true, 'statistics': stats}),
        headers: {'content-type': 'application/json'},
      );
    } catch (e) {
      return Response.internalServerError(
        body: jsonEncode({'success': false, 'error': e.toString()}),
        headers: {'content-type': 'application/json'},
      );
    }
  });

  // POST /api/demo/trigger (Simulate alert for instant phone testing)
  router.post('/api/demo/trigger', (Request request) async {
    try {
      final payload = await request.readAsString();
      final data = payload.isNotEmpty ? jsonDecode(payload) as Map<String, dynamic> : <String, dynamic>{};
      final type = (data['type'] ?? 'intruder').toString();

      Alert alert;
      if (type == 'baby_danger') {
        alert = await db.createAlert(
          alertType: 'baby_danger',
          title: 'NGUY HIỂM: BÉ CÓ NGUY CƠ RƠI KHỎI GIƯỜNG!',
          message: 'Bé đã bò ra sát mép giường/nôi an toàn trong 8 frames liên tiếp.',
          severity: 'CRITICAL',
          trackId: 1,
        );
        await db.updateSystemStatus(
          babyStatus: 'NGUY CO ROI KHOI GIUONG!',
          babyStatusColor: 'RED',
        );
      } else {
        alert = await db.createAlert(
          alertType: 'intruder',
          title: 'BÁO ĐỘNG ĐỘT NHẬP: PHÁT HIỆN NGƯỜI LẠ!',
          message: 'Hệ thống nhận diện phát hiện khuôn mặt không khớp với thành viên gia đình (Track #9).',
          severity: 'CRITICAL',
          trackId: 9,
        );
        await db.updateSystemStatus(
          securityStatus: 'BAO DONG: PHAT HIEN NGUOI LA!',
          securityStatusColor: 'RED',
        );
      }

      wsManager.broadcast('NEW_ALERT', alert.toJson());
      final status = await db.getSystemStatus();
      wsManager.broadcast('STATUS_UPDATE', status);

      return Response.ok(
        jsonEncode({
          'success': true,
          'message': 'Đã kích hoạt báo động mô phỏng thành công!',
          'alert': alert.toJson(),
        }),
        headers: {'content-type': 'application/json'},
      );
    } catch (e) {
      return Response.internalServerError(
        body: jsonEncode({'success': false, 'error': e.toString()}),
        headers: {'content-type': 'application/json'},
      );
    }
  });

  // ─────────────────────────────────────────────────────────────
  // FACE REGISTRATION ENDPOINTS
  // ─────────────────────────────────────────────────────────────

  // GET /api/faces  —  list registered persons
  router.get('/api/faces', (Request request) async {
    try {
      final familyDir = Directory(
        Platform.script.resolve('../../demo/family').toFilePath(),
      );
      // Fallback: look next to the demo project
      final demoPaths = [
        Platform.script.resolve('../../demo/family').toFilePath(),
        r'C:\Users\AD\Desktop\Downloads\baby_theft_demo_v2\baby_theft_demo\family',
      ];

      Directory? resolvedDir;
      for (final p in demoPaths) {
        final d = Directory(p);
        if (d.existsSync()) {
          resolvedDir = d;
          break;
        }
      }

      if (resolvedDir == null) {
        return Response.ok(
          jsonEncode({'success': true, 'persons': []}),
          headers: {'content-type': 'application/json'},
        );
      }

      final persons = resolvedDir
          .listSync()
          .whereType<Directory>()
          .map((d) {
            final name = d.path.split(Platform.pathSeparator).last;
            final count = d
                .listSync()
                .where((f) =>
                    f is File &&
                    (f.path.endsWith('.jpg') ||
                        f.path.endsWith('.jpeg') ||
                        f.path.endsWith('.png')))
                .length;
            return {'name': name, 'photo_count': count};
          })
          .toList();

      return Response.ok(
        jsonEncode({'success': true, 'persons': persons}),
        headers: {'content-type': 'application/json'},
      );
    } catch (e) {
      return Response.internalServerError(
        body: jsonEncode({'success': false, 'error': e.toString()}),
        headers: {'content-type': 'application/json'},
      );
    }
  });

  // POST /api/faces/register  —  receive base64 image, save to family/[name]/
  router.post('/api/faces/register', (Request request) async {
    try {
      final payload = await request.readAsString();
      final data = jsonDecode(payload) as Map<String, dynamic>;

      final name = (data['name'] ?? '').toString().trim();
      if (name.isEmpty) {
        return Response.badRequest(
          body: jsonEncode({'success': false, 'error': 'Tên không được để trống'}),
          headers: {'content-type': 'application/json'},
        );
      }

      // Sanitize name: only allow letters, numbers, underscore, space
      final safeName = name.replaceAll(RegExp(r'[^\w\s]'), '_').trim();

      final imageBase64 = (data['image'] ?? '').toString();
      if (imageBase64.isEmpty) {
        return Response.badRequest(
          body: jsonEncode({'success': false, 'error': 'Không có dữ liệu ảnh'}),
          headers: {'content-type': 'application/json'},
        );
      }

      // Decode base64 (strip data URL prefix if present)
      final base64Data = imageBase64.contains(',')
          ? imageBase64.split(',').last
          : imageBase64;
      final imageBytes = base64Decode(base64Data);

      // Find family directory
      final demoPaths = [
        r'C:\Users\AD\Desktop\Downloads\baby_theft_demo_v2\baby_theft_demo\family',
        Platform.script.resolve('../../demo/family').toFilePath(),
      ];

      String familyBasePath = demoPaths.first;
      for (final p in demoPaths) {
        final parent = Directory(p).parent;
        if (parent.existsSync()) {
          familyBasePath = p;
          break;
        }
      }

      final personDir = Directory('$familyBasePath${Platform.pathSeparator}$safeName');
      if (!personDir.existsSync()) {
        personDir.createSync(recursive: true);
      }

      // Count existing photos to generate index
      final existingCount = personDir
          .listSync()
          .where((f) =>
              f is File &&
              (f.path.endsWith('.jpg') ||
                  f.path.endsWith('.jpeg') ||
                  f.path.endsWith('.png')))
          .length;

      final photoIndex = existingCount + 1;
      final timestamp = DateTime.now().millisecondsSinceEpoch;
      final fileName = '${safeName}_${photoIndex}_$timestamp.jpg';
      final filePath = '${personDir.path}${Platform.pathSeparator}$fileName';

      File(filePath).writeAsBytesSync(imageBytes);

      print('[FaceRegister] Saved: $filePath (${imageBytes.length} bytes)');

      return Response.ok(
        jsonEncode({
          'success': true,
          'message': 'Đã lưu ảnh thành công!',
          'person': safeName,
          'photo_index': photoIndex,
          'file_name': fileName,
          'total_photos': photoIndex,
        }),
        headers: {'content-type': 'application/json'},
      );
    } catch (e) {
      print('[FaceRegister] Error: $e');
      return Response.internalServerError(
        body: jsonEncode({'success': false, 'error': e.toString()}),
        headers: {'content-type': 'application/json'},
      );
    }
  });

  // DELETE /api/faces/<name>  —  delete a registered person
  router.delete('/api/faces/<name>', (Request request, String name) async {
    try {
      final demoPaths = [
        r'C:\Users\AD\Desktop\Downloads\baby_theft_demo_v2\baby_theft_demo\family',
        Platform.script.resolve('../../demo/family').toFilePath(),
      ];

      for (final basePath in demoPaths) {
        final personDir = Directory('$basePath${Platform.pathSeparator}$name');
        if (personDir.existsSync()) {
          personDir.deleteSync(recursive: true);
          print('[FaceRegister] Deleted person: $name');
          return Response.ok(
            jsonEncode({'success': true, 'message': 'Đã xóa $name'}),
            headers: {'content-type': 'application/json'},
          );
        }
      }

      return Response.notFound(
        jsonEncode({'success': false, 'error': 'Không tìm thấy người: $name'}),
        headers: {'content-type': 'application/json'},
      );
    } catch (e) {
      return Response.internalServerError(
        body: jsonEncode({'success': false, 'error': e.toString()}),
        headers: {'content-type': 'application/json'},
      );
    }
  });

  // Combined handler: API & WS go to router, other requests go to Flutter Web PWA static files
  Future<Response> appHandler(Request request) async {
    final path = request.url.path;
    if (path.startsWith('api/') || path == 'ws') {
      return await router.call(request);
    }
    final response = await router.call(request);
    if (response.statusCode == 404) {
      return serveStatic(request);
    }
    return response;
  }

  // Middleware pipeline with CORS headers
  final pipeline = const Pipeline()
      .addMiddleware(corsHeaders(
        headers: {
          ACCESS_CONTROL_ALLOW_ORIGIN: '*',
          'Access-Control-Allow-Methods': 'GET, POST, PUT, DELETE, OPTIONS',
          'Access-Control-Allow-Headers': 'Origin, Content-Type, Accept, Authorization',
        },
      ))
      .addMiddleware(logRequests())
      .addHandler(appHandler);

  final server = await io.serve(pipeline, ip, port);
  print('====================================================');
  print('  BABY & ANTI-THEFT DART BACKEND SERVER RUNNING');
  print('  Listening on http://${server.address.host}:${server.port}');
  print('  WebSocket: ws://${server.address.host}:${server.port}/ws');
  print('  PostgreSQL: baby_security_db (user: postgres)');
  print('====================================================');
}
