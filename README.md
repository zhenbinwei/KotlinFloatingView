# KotlinFloatingView
### 如何引入

在项目根 build.gradle 添加
```
allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
```	
在使用的模块下添加
```
dependencies {
	        implementation 'com.github.zhenbinwei:KotlinFloatingView:1.0.0'
	}
```
需要使用的权限
```
<!-- 显示系统窗口权限 -->
<uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW"/>
<!-- 在 屏幕最顶部显示addview-->
<uses-permission android:name="android.permission.SYSTEM_OVERLAY_WINDOW" />
```
### 如何使用

```
//创建实例对象
FloatingWindow  floatingWindow=new FloatingWindow(this);
//设置展开的布局
floatingWindow.addRealContentView(View.inflate(this,R.layout.test,null));
//设置悬浮窗图标
floatingWindow.setMiniWindowIcon(R.mipmap.ic_launcher_round);
//显示
floatingWindow.addFloatingWindow();
//关闭
floatingWindow.removeFloatingWindow();
```
