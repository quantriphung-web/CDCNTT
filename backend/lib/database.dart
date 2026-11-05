import 'package:postgres/postgres.dart';
import 'models/alert.dart';

class DatabaseService {
  static final DatabaseService _instance = DatabaseService._internal();
  factory DatabaseService() => _instance;
  DatabaseService._internal();

  Connection? _connection;

  Future<Connection> getConnection() async {
    if (_connection != null && _connection!.isOpen) {
      return _connection!;
    }
    _connection = await Connection.open(
      Endpoint(
        host: 'localhost',
        port: 5432,
        database: 'baby_security_db',
        username: 'postgres',
        password: '123',
      ),
      settings: const ConnectionSettings(sslMode: SslMode.disable),
    );
    return _connection!;
  }

  Future<void> initDatabase() async {
    final conn = await getConnection();
    await conn.execute('''
      CREATE TABLE IF NOT EXISTS alerts (
        id SERIAL PRIMARY KEY,
        alert_type VARCHAR(50) NOT NULL,
        title VARCHAR(255) NOT NULL,
        message TEXT,
        severity VARCHAR(20) NOT NULL DEFAULT 'INFO',
        image_base64 TEXT,
        image_path VARCHAR(500),
        track_id INT,
        is_read BOOLEAN DEFAULT FALSE,
        is_resolved BOOLEAN DEFAULT FALSE,
        created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
      )
    ''');

    await conn.execute('''
      CREATE TABLE IF NOT EXISTS system_status (
        id SERIAL PRIMARY KEY,
        baby_status VARCHAR(50) DEFAULT 'TRE DANG THUC',
        baby_status_color VARCHAR(20) DEFAULT 'GREEN',
        security_status VARCHAR(50) DEFAULT 'AN TOAN',
        security_status_color VARCHAR(20) DEFAULT 'GREEN',
        is_camera_online BOOLEAN DEFAULT TRUE,
        active_mode VARCHAR(20) DEFAULT 'BOTH',
        last_snapshot_url TEXT,
        updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
      )
    ''');

    await conn.execute('''
      INSERT INTO system_status (baby_status, baby_status_color, security_status, security_status_color, is_camera_online, active_mode)
      SELECT 'TRE DANG THUC', 'GREEN', 'AN TOAN', 'GREEN', TRUE, 'BOTH'
      WHERE NOT EXISTS (SELECT 1 FROM system_status)
    ''');
    print('[Database] PostgreSQL connected and initialized successfully!');
  }

  Future<Alert> createAlert({
    required String alertType,
    required String title,
    required String message,
    required String severity,
    String? imageBase64,
    String? imagePath,
    int? trackId,
  }) async {
    final conn = await getConnection();
    final result = await conn.execute(
      Sql.named('''
        INSERT INTO alerts (alert_type, title, message, severity, image_base64, image_path, track_id)
        VALUES (@alertType, @title, @message, @severity, @imageBase64, @imagePath, @trackId)
        RETURNING id, alert_type, title, message, severity, image_base64, image_path, track_id, is_read, is_resolved, created_at;
      '''),
      parameters: {
        'alertType': alertType,
        'title': title,
        'message': message,
        'severity': severity,
        'imageBase64': imageBase64,
        'imagePath': imagePath,
        'trackId': trackId,
      },
    );

    final row = result.first;
    return Alert(
      id: row[0] as int,
      alertType: row[1] as String,
      title: row[2] as String,
      message: (row[3] ?? '') as String,
      severity: (row[4] ?? 'INFO') as String,
      imageBase64: row[5] as String?,
      imagePath: row[6] as String?,
      trackId: row[7] as int?,
      isRead: (row[8] ?? false) as bool,
      isResolved: (row[9] ?? false) as bool,
      createdAt: row[10] as DateTime,
    );
  }

  Future<List<Alert>> getAlerts({
    int limit = 50,
    int offset = 0,
    String? type,
    bool? unreadOnly,
  }) async {
    final conn = await getConnection();
    String query = '''
      SELECT id, alert_type, title, message, severity, image_base64, image_path, track_id, is_read, is_resolved, created_at
      FROM alerts
      WHERE 1=1
    ''';
    final params = <String, dynamic>{
      'limit': limit,
      'offset': offset,
    };

    if (type != null && type.isNotEmpty && type != 'ALL') {
      query += ' AND alert_type = @type';
      params['type'] = type;
    }
    if (unreadOnly == true) {
      query += ' AND is_read = false';
    }

    query += ' ORDER BY created_at DESC LIMIT @limit OFFSET @offset;';

    final result = await conn.execute(Sql.named(query), parameters: params);

    return result.map((row) {
      return Alert(
        id: row[0] as int,
        alertType: row[1] as String,
        title: row[2] as String,
        message: (row[3] ?? '') as String,
        severity: (row[4] ?? 'INFO') as String,
        imageBase64: row[5] as String?,
        imagePath: row[6] as String?,
        trackId: row[7] as int?,
        isRead: (row[8] ?? false) as bool,
        isResolved: (row[9] ?? false) as bool,
        createdAt: row[10] as DateTime,
      );
    }).toList();
  }

  Future<bool> markAsRead(int alertId) async {
    final conn = await getConnection();
    final res = await conn.execute(
      Sql.named('UPDATE alerts SET is_read = true WHERE id = @id;'),
      parameters: {'id': alertId},
    );
    return res.affectedRows > 0;
  }

  Future<bool> markAsResolved(int alertId) async {
    final conn = await getConnection();
    final res = await conn.execute(
      Sql.named('UPDATE alerts SET is_resolved = true, is_read = true WHERE id = @id;'),
      parameters: {'id': alertId},
    );
    return res.affectedRows > 0;
  }

  Future<bool> deleteAlert(int alertId) async {
    final conn = await getConnection();
    final res = await conn.execute(
      Sql.named('DELETE FROM alerts WHERE id = @id;'),
      parameters: {'id': alertId},
    );
    return res.affectedRows > 0;
  }

  Future<bool> clearAllAlerts() async {
    final conn = await getConnection();
    final res = await conn.execute('DELETE FROM alerts;');
    return res.affectedRows >= 0;
  }

  Future<Map<String, dynamic>> getSystemStatus() async {
    final conn = await getConnection();
    final res = await conn.execute(
      'SELECT baby_status, baby_status_color, security_status, security_status_color, is_camera_online, active_mode, last_snapshot_url, updated_at FROM system_status ORDER BY id ASC LIMIT 1;',
    );
    if (res.isEmpty) {
      return {
        'baby_status': 'TRE DANG THUC',
        'baby_status_color': 'GREEN',
        'security_status': 'AN TOAN',
        'security_status_color': 'GREEN',
        'is_camera_online': true,
        'active_mode': 'BOTH',
      };
    }
    final r = res.first;
    return {
      'baby_status': r[0] ?? 'TRE DANG THUC',
      'baby_status_color': r[1] ?? 'GREEN',
      'security_status': r[2] ?? 'AN TOAN',
      'security_status_color': r[3] ?? 'GREEN',
      'is_camera_online': r[4] ?? true,
      'active_mode': r[5] ?? 'BOTH',
      'last_snapshot_url': r[6],
      'updated_at': (r[7] as DateTime?)?.toIso8601String(),
    };
  }

  Future<void> updateSystemStatus({
    String? babyStatus,
    String? babyStatusColor,
    String? securityStatus,
    String? securityStatusColor,
    bool? isCameraOnline,
    String? activeMode,
    String? lastSnapshotUrl,
  }) async {
    final conn = await getConnection();
    await conn.execute(
      Sql.named('''
        UPDATE system_status SET
          baby_status = COALESCE(@babyStatus, baby_status),
          baby_status_color = COALESCE(@babyStatusColor, baby_status_color),
          security_status = COALESCE(@securityStatus, security_status),
          security_status_color = COALESCE(@securityStatusColor, security_status_color),
          is_camera_online = COALESCE(@isCameraOnline, is_camera_online),
          active_mode = COALESCE(@activeMode, active_mode),
          last_snapshot_url = COALESCE(@lastSnapshotUrl, last_snapshot_url),
          updated_at = CURRENT_TIMESTAMP
        WHERE id = 1;
      '''),
      parameters: {
        'babyStatus': babyStatus,
        'babyStatusColor': babyStatusColor,
        'securityStatus': securityStatus,
        'securityStatusColor': securityStatusColor,
        'isCameraOnline': isCameraOnline,
        'activeMode': activeMode,
        'lastSnapshotUrl': lastSnapshotUrl,
      },
    );
  }

  Future<Map<String, dynamic>> getStatistics() async {
    final conn = await getConnection();
    final totalRes = await conn.execute('SELECT COUNT(*) FROM alerts;');
    final unreadRes = await conn.execute('SELECT COUNT(*) FROM alerts WHERE is_read = false;');
    final intruderRes = await conn.execute("SELECT COUNT(*) FROM alerts WHERE alert_type IN ('intruder', 'theft', 'stranger');");
    final babyDangerRes = await conn.execute("SELECT COUNT(*) FROM alerts WHERE alert_type IN ('baby_danger', 'fall');");

    return {
      'total_alerts': totalRes.first[0] as int,
      'unread_alerts': unreadRes.first[0] as int,
      'intruder_alerts': intruderRes.first[0] as int,
      'baby_danger_alerts': babyDangerRes.first[0] as int,
    };
  }
}
