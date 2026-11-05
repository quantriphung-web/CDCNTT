import 'dart:convert';
import 'package:http/http.dart' as http;
import '../models/alert_item.dart';

class ApiService {
  // Mặc định kết nối tới IP WiFi máy tính: 192.168.100.225:8080
  static String baseUrl = 'http://192.168.100.225:8080';

  static void setBaseUrl(String url) {
    if (url.endsWith('/')) {
      baseUrl = url.substring(0, url.length - 1);
    } else {
      baseUrl = url;
    }
  }

  // ── Face Registration ────────────────────────────────────────

  /// Gửi một ảnh base64 lên backend để đăng ký khuôn mặt [name].
  /// Trả về Map chứa {success, photo_index, total_photos, ...}
  static Future<Map<String, dynamic>> registerFacePhoto({
    required String name,
    required String base64Image,
  }) async {
    try {
      final uri = Uri.parse('$baseUrl/api/faces/register');
      final response = await http.post(
        uri,
        headers: {'Content-Type': 'application/json'},
        body: jsonEncode({'name': name, 'image': base64Image}),
      ).timeout(const Duration(seconds: 30));
      return jsonDecode(response.body) as Map<String, dynamic>;
    } catch (e) {
      return {'success': false, 'error': e.toString()};
    }
  }

  /// Lấy danh sách người đã đăng ký khuôn mặt.
  static Future<List<Map<String, dynamic>>> fetchRegisteredFaces() async {
    try {
      final uri = Uri.parse('$baseUrl/api/faces');
      final response = await http.get(uri).timeout(const Duration(seconds: 5));
      if (response.statusCode == 200) {
        final data = jsonDecode(response.body) as Map<String, dynamic>;
        return List<Map<String, dynamic>>.from(data['persons'] ?? []);
      }
    } catch (e) {
      print('[ApiService] Error fetchRegisteredFaces: $e');
    }
    return [];
  }

  /// Xóa một người đã đăng ký.
  static Future<bool> deleteRegisteredFace(String name) async {
    try {
      final uri = Uri.parse('$baseUrl/api/faces/$name');
      final response = await http.delete(uri).timeout(const Duration(seconds: 5));
      return response.statusCode == 200;
    } catch (e) {
      return false;
    }
  }

  // ── Alerts ───────────────────────────────────────────────────

  static Future<List<AlertItem>> fetchAlerts({String? type}) async {
    try {
      final uri = Uri.parse('$baseUrl/api/alerts${type != null && type != 'ALL' ? '?type=$type' : ''}');
      final response = await http.get(uri).timeout(const Duration(seconds: 5));
      if (response.statusCode == 200) {
        final data = jsonDecode(response.body) as Map<String, dynamic>;
        final list = (data['alerts'] as List? ?? [])
            .map((item) => AlertItem.fromJson(item as Map<String, dynamic>))
            .toList();
        return list;
      }
    } catch (e) {
      print('[ApiService] Error fetchAlerts: $e');
    }
    return [];
  }

  static Future<Map<String, dynamic>> fetchStatus() async {
    try {
      final uri = Uri.parse('$baseUrl/api/status');
      final response = await http.get(uri).timeout(const Duration(seconds: 5));
      if (response.statusCode == 200) {
        final data = jsonDecode(response.body) as Map<String, dynamic>;
        return data['status'] as Map<String, dynamic>? ?? {};
      }
    } catch (e) {
      print('[ApiService] Error fetchStatus: $e');
    }
    return {};
  }

  static Future<Map<String, dynamic>> fetchStatistics() async {
    try {
      final uri = Uri.parse('$baseUrl/api/statistics');
      final response = await http.get(uri).timeout(const Duration(seconds: 5));
      if (response.statusCode == 200) {
        final data = jsonDecode(response.body) as Map<String, dynamic>;
        return data['statistics'] as Map<String, dynamic>? ?? {};
      }
    } catch (e) {
      print('[ApiService] Error fetchStatistics: $e');
    }
    return {};
  }

  static Future<bool> markAlertAsRead(int id) async {
    try {
      final uri = Uri.parse('$baseUrl/api/alerts/$id/read');
      final response = await http.post(uri).timeout(const Duration(seconds: 4));
      return response.statusCode == 200;
    } catch (e) {
      return false;
    }
  }

  static Future<bool> markAlertAsResolved(int id) async {
    try {
      final uri = Uri.parse('$baseUrl/api/alerts/$id/resolve');
      final response = await http.post(uri).timeout(const Duration(seconds: 4));
      return response.statusCode == 200;
    } catch (e) {
      return false;
    }
  }

  static Future<bool> deleteAlert(int id) async {
    try {
      final uri = Uri.parse('$baseUrl/api/alerts/$id');
      final response = await http.delete(uri).timeout(const Duration(seconds: 4));
      return response.statusCode == 200;
    } catch (e) {
      print('[ApiService] Error deleteAlert: $e');
      return false;
    }
  }

  static Future<bool> clearAllAlerts() async {
    try {
      final uri = Uri.parse('$baseUrl/api/alerts');
      final response = await http.delete(uri).timeout(const Duration(seconds: 5));
      return response.statusCode == 200;
    } catch (e) {
      print('[ApiService] Error clearAllAlerts: $e');
      return false;
    }
  }

  static Future<bool> triggerDemoAlert(String type) async {
    try {
      final uri = Uri.parse('$baseUrl/api/demo/trigger');
      final response = await http.post(
        uri,
        headers: {'Content-Type': 'application/json'},
        body: jsonEncode({'type': type}),
      ).timeout(const Duration(seconds: 5));
      return response.statusCode == 200;
    } catch (e) {
      print('[ApiService] Error triggerDemoAlert: $e');
      return false;
    }
  }
}
