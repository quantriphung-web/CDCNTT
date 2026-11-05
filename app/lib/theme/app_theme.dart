import 'package:flutter/material.dart';

/// Shared design tokens for the entire app
class AppTheme {
  // Background layers
  static const bg0 = Color(0xFF0A0D14);   // deepest bg
  static const bg1 = Color(0xFF12161F);   // scaffold bg
  static const bg2 = Color(0xFF1A1F2E);   // card bg
  static const bg3 = Color(0xFF222840);   // elevated card

  // Accent
  static const blue   = Color(0xFF4F8EF7);
  static const purple = Color(0xFF8B5CF6);
  static const green  = Color(0xFF34D399);
  static const red    = Color(0xFFEF4444);
  static const amber  = Color(0xFFF59E0B);
  static const orange = Color(0xFFF97316);
  static const teal   = Color(0xFF14B8A6);

  // Text
  static const textPrimary   = Color(0xFFF1F5F9);
  static const textSecondary = Color(0xFF94A3B8);
  static const textMuted     = Color(0xFF475569);

  static const cardRadius    = 20.0;
  static const smallRadius   = 12.0;

  static LinearGradient blueGradient = const LinearGradient(
    colors: [Color(0xFF4F8EF7), Color(0xFF8B5CF6)],
    begin: Alignment.topLeft,
    end: Alignment.bottomRight,
  );

  static LinearGradient greenGradient = const LinearGradient(
    colors: [Color(0xFF34D399), Color(0xFF059669)],
    begin: Alignment.topLeft,
    end: Alignment.bottomRight,
  );

  static LinearGradient redGradient = const LinearGradient(
    colors: [Color(0xFFEF4444), Color(0xFFB91C1C)],
    begin: Alignment.topLeft,
    end: Alignment.bottomRight,
  );

  static LinearGradient amberGradient = const LinearGradient(
    colors: [Color(0xFFF59E0B), Color(0xFFD97706)],
    begin: Alignment.topLeft,
    end: Alignment.bottomRight,
  );

  static BoxDecoration glassMorphism({
    Color borderColor = Colors.white,
    double borderOpacity = 0.08,
    double bgOpacity = 0.05,
  }) {
    return BoxDecoration(
      color: Colors.white.withOpacity(bgOpacity),
      borderRadius: BorderRadius.circular(cardRadius),
      border: Border.all(color: borderColor.withOpacity(borderOpacity)),
    );
  }
}
