import 'package:flutter_test/flutter_test.dart';
import 'package:baby_security_app/main.dart';

void main() {
  testWidgets('App loads smoke test', (WidgetTester tester) async {
    await tester.pumpWidget(const BabySecurityApp());
    expect(find.byType(BabySecurityApp), findsOneWidget);
  });
}
