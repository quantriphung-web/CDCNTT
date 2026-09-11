import 'package:flutter/material.dart';
import 'package:flutter/widgets.dart';
import 'package:food_delivery_app/pages/splash_page.dart';
import 'package:food_delivery_app/values/app_assets.dart';
import 'package:food_delivery_app/values/app_colors.dart';
import 'package:get/get.dart';

class HomePage extends StatefulWidget {
  const HomePage({super.key});

  @override
  State<HomePage> createState() => _HomePageState();
}

class _HomePageState extends State<HomePage> {
  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
          leading: GestureDetector(
            child: Container(
              child: Image.asset(AppAssets.menuIcon),
              padding: const EdgeInsets.all(10),
              decoration: BoxDecoration(
                  borderRadius: BorderRadius.circular(50),
                  color: AppColors.bgButtonColor),
            ),
          ),
          title: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(
                'DELIVER TO',
                style: TextStyle(color: AppColors.primaryColor, fontSize: 16),
              ),
              GestureDetector(
                  onTap: () {},
                  child: Row(
                    children: [
                      Padding(
                        padding: const EdgeInsets.only(right: 10),
                        child: Text(
                          'Binh ne',
                          style: TextStyle(fontSize: 14),
                        ),
                      ),
                      Image.asset(
                        AppAssets.polygonIcon,
                        width: 14,
                      )
                    ],
                  ))
            ],
          )),
      body: Center(
        child: TextButton(
          child: Text('haha'),
          onPressed: () {},
        ),
      ),
    );
  }
}
