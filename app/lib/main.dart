import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'screens/home_screen.dart';
import 'services/notification_service.dart';
import 'theme/app_theme.dart';

void main() async {
  WidgetsFlutterBinding.ensureInitialized();
  // Init push notifications (mobile only)
  if (!kIsWeb) {
    await NotificationService.initialize();
  }
  runApp(const BabySecurityApp());
}

class BabySecurityApp extends StatelessWidget {
  const BabySecurityApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Baby & SafeHome Security AI',
      debugShowCheckedModeBanner: false,
      themeMode: ThemeMode.dark,
      darkTheme: ThemeData(
        brightness: Brightness.dark,
        scaffoldBackgroundColor: AppTheme.bg1,
        colorScheme: const ColorScheme.dark(
          primary: AppTheme.blue,
          secondary: AppTheme.purple,
          error: AppTheme.red,
          surface: AppTheme.bg2,
        ),
        appBarTheme: const AppBarTheme(
          backgroundColor: AppTheme.bg0,
          elevation: 0,
        ),
        useMaterial3: true,
      ),
      home: const HomeScreen(),
    );
  }
}
