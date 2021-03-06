# ScratchView
Android 自定义 View 实现刮刮卡效果
## 效果
![image](screen-snap/scratch.gif)

## 用法
Step 1. Add the JitPack repository to your build file
Add it in your root build.gradle at the end of repositories:
```
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
```
Step 2. Add the dependency
```
dependencies {
    implementation 'com.github.MaxPanda0206:ScratchView:1.0.1'
}
```
XML
```
<androidx.constraintlayout.widget.ConstraintLayout
        android:layout_width="match_parent"
        android:layout_height="match_parent">
        <!-- 底部 -->
        <TextView
            android:id="@+id/content_view"
            android:layout_width="match_parent"
            android:layout_height="0dp"
            android:gravity="center"
            android:text="谢谢惠顾"
            android:textColor="@color/colorAccent"
            android:textSize="40sp"
            android:textStyle="bold"
            app:layout_constraintDimensionRatio="H,2:1"
            app:layout_constraintTop_toTopOf="parent"
            tools:ignore="HardcodedText" />
        <!-- 表面 -->
        <cn.com.maxpanda.view.ScratchView
            android:id="@+id/scratch_view"
            android:layout_width="match_parent"
            android:layout_height="0dp"
            app:surfaceDrawMode="normal"
            app:surfaceColor="@color/colorPrimary"
            app:surfaceImageSrc="@mipmap/scratch_bg"
            app:eraseSize="60"
            app:layout_constraintDimensionRatio="H,2:1"
            app:layout_constraintTop_toTopOf="parent">

        </cn.com.maxpanda.view.ScratchView>
    </androidx.constraintlayout.widget.ConstraintLayout>
```
JAVA

```
ScratchView scratchView = new ScratchView(this);
//最大擦除系数 达到该系数会调用 OnEraseStatusListener 的 onCompleted() 方法
scratchView.setMaxPercent(0.8f); // 0f ～ 1f
//橡皮擦大小
scratchView.setEraseSize(60f);
//涂层图片资源 ID
scratchView.setSurfaceImageSrcId(R.mipmap.scratch_bg);
//涂层绘制模式
scratchView.setSurfaceDrawMode(ScratchView.Mode.WATERMARK);// ScratchView.Mode.NORMAL | ScratchView.Mode.WATERMARK
//涂层颜色
scratchView.setSurfaceColor(Color.RED);
//擦除状态监听器
scratchView.setOnEraseStatusListener(new ScratchView.OnEraseStatusListener() {
    @Override
    public void onProgress(float percent) {
        
    }

    @Override
    public void onCompleted(View view) {

    }
});
```
## 感谢 
[D_clock爱吃葱花： https://github.com/D-clock/ScratchView](https://github.com/D-clock/ScratchView/)  
[Harish Sridharan： https://github.com/sharish/ScratchView](https://github.com/sharish/ScratchView)
