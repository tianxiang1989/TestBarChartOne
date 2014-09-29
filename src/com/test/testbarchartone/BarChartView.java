package com.test.testbarchartone;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.text.Layout.Alignment;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.TypedValue;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;

/**
 * 柱状图view
 * @author liuxiuquan
 * 2014-9-19
 */
public class BarChartView extends View {
	/** 打印log信息时的标识 */
	private final String TAG = BarChartView.class.getSimpleName();

	// ===============各种常量===============
	/**枚举：标识传来的x数据中最大值和最小值的正负的
	 * BOTH_POSITIVE:最大值最小值均为正数
	 * BOTH_NEGATIVE:最大值最小值均为负数
	 * POSITIVE_NEGATIVE:最大值为正数，最小值为负数
	 * */
	public enum POSITIVE_FLAG {
		BOTH_POSITIVE, BOTH_NEGATIVE, POSITIVE_NEGATIVE
	}

	/**view的间隔 */
	private final int VIEW_MARGIN = changeDp(0);
	/**x坐标几等分*/
	private final int X_HOW_MANY = 5;
	/** chart标题栏的高度 */
	private final float CHART_TITLE_ROW_HEIHGT = changeDp(50);
	/**getBaseLine中用到的标识 垂直居中*/
	private final int TEXT_ALIGN_CENTER = 0;
	/**getBaseLine中用到的标识 垂直靠下*/
	private final int TEXT_ALIGN_BOTTOM = 1;
	/**柱状图背景的x左坐标位置*/
	private final int barChartViewBgnLeft = changeDp(66);// 这个常量没有大写，为了和barChartViewBgnRight保持外观一致...
	/**chart图表部分的内侧右边距*/
	private final int PADDING_RIGHT = changeDp(40);
	/**柱状图文字的边距*/
	private final int TEXT_PADDING = changeDp(4);
	/**柱状图的高度*/
	private final int BARCHART_HEIGHT = changeDp(18);
	/**每个柱状图加上下面的空白的高度*/
	private final int BARCHART_UP_DOWN_HEIGHT = changeDp(30);
	// ===============view状态变量===============
	/**柱状图背景的x右坐标*/
	private float barChartViewBgnRight;
	/**view的宽度 */
	private int viewWidth;
	/**view的高度 */
	private int viewHeight;
	/**x=0的位置*/
	private float zeroXAxis = 0f;
	/**y轴有几个名称*/
	int sizeY;
	/**容器*/
	Context context;
	// ===============各种画笔===============
	/**背景的画笔*/
	private Paint backGroundPaint;
	/**轴的画笔*/
	private Paint axisPaint;
	/**画x轴数值的textPaint画笔*/
	TextPaint textXYPaint;
	/**画标题的画笔*/
	private Paint titlePaint;
	/**背景参考柱状图的画笔*/
	private Paint relativeBarchartPaint;
	/**画x参考线的画笔*/
	private Paint relativeXLinePaint;
	/**柱状图画笔*/
	private Paint barchartPaint;
	/**画x=0竖线的画笔*/
	private Paint zeroXPaint;
	/**画x=0数值的画笔*/
	private TextPaint zeroXTextPaint;
	/**柱状图文字的画笔*/
	private TextPaint textBarchartPaint;
	// ===============view数据===============
	/** chart标题名称 */
	private String chartTitleName;

	/** y轴的数据 */
	private List<String> yValueList = new ArrayList<String>();
	/** y轴坐标 [左上角的点] */
	private List<Float> yAxisList = new ArrayList<Float>();

	/** x轴的数据 */
	private List<Float> xValueList = new ArrayList<Float>();
	/** x轴坐标 [左上角的点] */
	private List<Float> xAxisList = new ArrayList<Float>();

	/** x轴坐标 分割线的x坐标值 */
	private List<Float> xAxisLineList = new ArrayList<Float>();
	/** x轴坐标 分割线的x显示值 */
	private List<String> xAxisLineValueList = new ArrayList<String>();
	// ===============各种标识变量===============
	/** 是否已经加载完服务器传来的数据--控制是否调用onDraw方法 */
	private boolean runDraw = false;
	/**标识传来的x数据中最大值和最小值的正负*/
	POSITIVE_FLAG positive_Flag;

	// ---------------各种方法BEGIN---------------
	// ---------------复写父类的方法---------------
	/**
	 * 构造方法
	 * @param context 容器
	 * @param size y轴有几个名称
	 */
	public BarChartView(Context context, int size) {
		super(context);
		this.context = context;
		sizeY = size;
		initPaint(); // 初始化画笔
	}

	/**组件自适应大小*/
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		Display display = manager.getDefaultDisplay();
		viewWidth = display.getWidth();// 定义宽
		viewHeight = (int) CHART_TITLE_ROW_HEIHGT + PADDING_RIGHT + BARCHART_UP_DOWN_HEIGHT * sizeY
				+ changeDp(10);// 定义高
		barChartViewBgnRight = this.viewWidth - VIEW_MARGIN - PADDING_RIGHT;
		setMeasuredDimension(viewWidth, viewHeight);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		if (runDraw) {// 数据已加载完毕
			super.onDraw(canvas);
			init();// 数据初始化
			drawOutsideBackground(canvas); // 画外面的背景
			drawTitleText(canvas); // 画标题
			drawYBackgroud(canvas); // 画柱状图的参考背景
			drawYText(canvas);// 画y轴显示的值
			drawXRelativeLineAndText(canvas);// 画x轴参考竖线以及上方显示的值
			if (zeroXAxis != 0) {// 判断是否需要画x=0参考线和值
				drawXZeroLineAndText(canvas);
			}
			drawXBarChart(canvas);// 画x轴的柱状图
		}
	}

	// ---------------初始化数据的方法---------------
	/** 初始化画笔 */
	public void initPaint() {
		// 背景的画笔
		backGroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		backGroundPaint.setAntiAlias(true);
		backGroundPaint.setColor(Color.WHITE);

		// 轴的画笔
		axisPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		axisPaint.setColor(0xffc6c6c6);
		axisPaint.setTextSize(changeDp(12));

		// 画x,y值数值的画笔
		textXYPaint = new TextPaint();
		textXYPaint.setTextSize(changeDp(12));
		textXYPaint.setColor(0xffc3c3c3);

		// 画标题的画笔
		titlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		titlePaint.setAntiAlias(true);
		titlePaint.setColor(0xff7d7d7d);
		// titlePaint.setAlpha(90);//透明度
		titlePaint.setTextSize(changeDp(16));

		// 背景参考柱状图的画笔
		relativeBarchartPaint = new Paint();
		relativeBarchartPaint.setColor(0xfff0f7fd);// 淡蓝色的参考柱状图背景色
		relativeBarchartPaint.setAntiAlias(true);
		relativeBarchartPaint.setStyle(Paint.Style.FILL);

		// 画x参考线的画笔
		relativeXLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		relativeXLinePaint.setAntiAlias(true);
		relativeXLinePaint.setColor(Color.BLACK);
		relativeXLinePaint.setAlpha(80);

		// 柱状图画笔
		barchartPaint = new Paint();
		barchartPaint.setColor(0xff1ea8ff);// 蓝色的柱状图
		barchartPaint.setAntiAlias(true);
		barchartPaint.setStyle(Paint.Style.FILL);

		// 画x=0竖线的画笔
		zeroXPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		zeroXPaint.setAntiAlias(true);
		zeroXPaint.setColor(Color.BLACK);
		zeroXPaint.setAlpha(95);
		// zeroXPaint.setStrokeWidth(changeDp(2));

		// 画x=0数值的画笔
		zeroXTextPaint = new TextPaint();
		zeroXTextPaint.setTextSize(changeDp(16));
		zeroXTextPaint.setColor(Color.RED);

		// 柱状图文字的画笔
		textBarchartPaint = new TextPaint();
		textBarchartPaint.setTextSize(changeDp(12));
		textBarchartPaint.setColor(0xff1ea8ff);
	}

	/**初始化数据*/
	private void init() {
		// 1 计算y相关数据
		calYAxisList();
		// 2 计算x相关数据
		calXAxisList();
	}

	// ---------------计算数值的方法---------------
	/**计算y相关的东东*/
	private void calYAxisList() {
		float startY = CHART_TITLE_ROW_HEIHGT + changeDp(40);
		for (int i = 0; i < yValueList.size(); i++) {
			yAxisList.add(startY);
			startY = startY + BARCHART_UP_DOWN_HEIGHT;
		}
	}

	/**
	 * 计算x值的东东
	 */
	private void calXAxisList() {
		/**x值的最小值*/
		float xMinValue = getXMinValue();
		/**x值的最大值*/
		float xMaxValue = getXMaxValue();
		/**x最小值的宽度*/
		float xMaxValueWidth = axisPaint.measureText(xMaxValue + "") + TEXT_PADDING;
		/**x最大值的宽度*/
		float xMinValueWidth = axisPaint.measureText(xMinValue + "") + TEXT_PADDING;
		/**柱状图背景图的宽度*/
		float barCharViewBgnWidth = barChartViewBgnRight - barChartViewBgnLeft;
		/**精度比例*/
		float degree;
		/**画的x最大值*/
		float xDrawMaxValue;
		/**画的x最小值*/
		float xDrawMinValue;
		/**x坐标的间距*/
		float perSpaceAxisValue = barCharViewBgnWidth / X_HOW_MANY;
		/**传来数组中x的值*/
		float xValue;
		/**传来数组中x的坐标*/
		float xAlis;

		judgPositive(xMinValue, xMaxValue);// 判断最大最小值的正负
		switch (positive_Flag) {
		case BOTH_POSITIVE:// 最大最小值均为正值
			// 思路：从x的最小值计算各个点
			if ((xMaxValue - xMinValue) / 2f > xMinValue) {// 最大值远大于最小值
				xDrawMinValue = 0;
			} else {
				xDrawMinValue = xMinValue - xMinValue * 0.01f;
				// xDrawMinValue = xMinValue / 1.01f;
			}
			degree = (xMaxValue - xDrawMinValue) / (barCharViewBgnWidth - xMaxValueWidth);

			// 计算x各个值的坐标

			for (int j = 0; j < xValueList.size(); j++) {
				xValue = xValueList.get(j);
				xAlis = barChartViewBgnLeft + (xValue - xDrawMinValue) / degree;
				xAxisList.add(xAlis);
			}

			// 计算x等分点参考线显示的值
			xAxisLineValueList.clear();
			for (int i = 0; i <= X_HOW_MANY; i++) {
				xAxisLineValueList.add(formatNum(xDrawMinValue));
				xDrawMinValue = xDrawMinValue + perSpaceAxisValue * degree;
			}
			break;
		case BOTH_NEGATIVE:// 最大值最小值均为负值
			// 思路:从x的最大值计算各个点
			if ((xMinValue - xMaxValue) / 2f < xMaxValue) {
				xDrawMaxValue = 0;
			} else {
				xDrawMaxValue = xMaxValue - xMaxValue * 0.01f;
//				 xDrawMaxValue = xMaxValue / 1.05f;
			}
			degree = (xDrawMaxValue - xMinValue) / (barCharViewBgnWidth - xMinValueWidth);

			// 计算x各个值的坐标
			for (int j = 0; j < xValueList.size(); j++) {
				xValue = xValueList.get(j);
				xAlis = barChartViewBgnRight - (xDrawMaxValue - xValue) / degree;
				xAxisList.add(xAlis);
			}
			// 计算x等分点参考线显示的值
			xAxisLineValueList.clear();
			for (int i = 0; i <= X_HOW_MANY; i++) {
				xAxisLineValueList.add(formatNum(xDrawMaxValue));
				xDrawMaxValue = xDrawMaxValue - perSpaceAxisValue * degree;
			}
			Collections.reverse(xAxisLineValueList);// list翻转
			break;
		case POSITIVE_NEGATIVE:// 最大值为正值，最小值为负值
			// 思路：求出最左边的线，然后计算出各个点
			degree = (xMaxValue - xMinValue)
					/ (barCharViewBgnWidth - xMaxValueWidth - xMinValueWidth);
			xDrawMinValue = xMinValue - xMinValueWidth * degree;// 最左边的点

			// 传过来的数组中x最小值的坐标
			float xMinAxis = barChartViewBgnLeft + xMinValueWidth;
			zeroXAxis = -xMinValue / degree + xMinAxis;// 计算x=0的点的坐标

			// 计算x各个值的坐标
			for (int j = 0; j < xValueList.size(); j++) {
				xValue = xValueList.get(j);
				xAlis = zeroXAxis + (xValue) / degree;
				xAxisList.add(xAlis);
			}

			// 计算x等分点参考线显示的值
			xAxisLineValueList.clear();
			for (int i = 0; i <= X_HOW_MANY; i++) {
				xAxisLineValueList.add(formatNum(xDrawMinValue));
				xDrawMinValue = xDrawMinValue + perSpaceAxisValue * degree;
			}
			break;
		}

	}

	/**判断最大最小值的正负*/
	private void judgPositive(float xMinValue, float xMaxValue) {
		if (xMinValue >= 0) {// 最大最小值均为正值
			positive_Flag = POSITIVE_FLAG.BOTH_POSITIVE;
		} else if (xMaxValue < 0) {// 最大值最小值均为负值
			positive_Flag = POSITIVE_FLAG.BOTH_NEGATIVE;
		} else if (xMaxValue >= 0 && xMinValue < 0) {// 最大值为正值，最小值为负值
			positive_Flag = POSITIVE_FLAG.POSITIVE_NEGATIVE;
		}
	}

	/**获得x轴的最小值 [服务器的数据时排序过传来的，其实xValueList的最后一个值就是最小值]*/
	private float getXMinValue() {
		float localXMinValue = xValueList.size() == 0 ? 0 : xValueList.get(xValueList.size() - 1);
		// Log.v(TAG, "localXMinValue==" + localXMinValue);
		return localXMinValue;
	}

	/**获得x轴的最大值 [服务器的数据时排序过传来的，其实xValueList的第一个值就是最大值]*/
	private float getXMaxValue() {
		float localXMaxValue = xValueList.size() == 0 ? 0 : xValueList.get(0);
		// Log.v(TAG, "localXMaxValue==" + localXMaxValue);
		return localXMaxValue;
	}

	// ---------------控制状态的方法---------------

	/**数据就位，开始画图(调用onDraw方法)*/
	public void startRunDraw() {
		runDraw = true;
		postInvalidate();
	}

	/**
	 * 设置y轴的数据
	 * @param yValueList
	 */
	public void setyValueList(List<String> yValueList) {
		this.yValueList = yValueList;
	}

	/**
	 * 设置x轴的数据
	 * @param xValueList
	 */
	public void setxValueList(List<Float> xValueList) {
		this.xValueList = xValueList;
	}

	/**
	 * 设置报表的标题
	 * @param chartTitleName 标题
	 */
	public void setChartTitleName(String chartTitleName) {
		this.chartTitleName = chartTitleName;
	}

	// ---------------画图的方法---------------

	/**画最外面的背景*/
	private void drawOutsideBackground(Canvas canvas) {
		int rectLeft = VIEW_MARGIN;
		int rectTop = VIEW_MARGIN;
		int rectRight = this.viewWidth - VIEW_MARGIN;
		int rectBottom = this.viewHeight - VIEW_MARGIN;
		RectF backGroundRect = new RectF(rectLeft, rectTop, rectRight, rectBottom);
		canvas.drawRoundRect(backGroundRect, 0, 0, backGroundPaint);
	}

	/**画标题*/
	private void drawTitleText(Canvas canvas) {
		// 画标题下的一条横线
		float lineStartX = changeDp(10);
		float lineStartY = CHART_TITLE_ROW_HEIHGT;
		float lineStopX = this.viewWidth - changeDp(20);
		float lineStopY = lineStartY;
		canvas.drawLine(lineStartX, lineStartY, lineStopX, lineStopY, titlePaint);

		float rectLeft = VIEW_MARGIN;
		float rectTop = VIEW_MARGIN;
		float rectRight = this.viewWidth - VIEW_MARGIN;
		float rectBottom = CHART_TITLE_ROW_HEIHGT;
		RectF textRect = new RectF(rectLeft, rectTop, rectRight, rectBottom);
		float baseline = getBaseLine(titlePaint, textRect, TEXT_ALIGN_CENTER);// 纵向居中
		titlePaint.setTextAlign(Paint.Align.CENTER);// 水平居中
		canvas.drawText(chartTitleName, textRect.centerX(), baseline, titlePaint);
	}

	/**画参考柱状图的背景色*/
	private void drawYBackgroud(Canvas canvas) {
		for (int i = 0; i < yValueList.size(); i++) {
			RectF bar = new RectF(barChartViewBgnLeft, yAxisList.get(i), barChartViewBgnRight,
					yAxisList.get(i) + changeDp(18));
			canvas.drawRect(bar, relativeBarchartPaint);
		}
	}

	/**画Y轴上显示的值*/
	private void drawYText(Canvas canvas) {
		// 画y轴数值
		for (int i = 0; i < yValueList.size(); i++) {
			float yAxis = yAxisList.get(i);
			String str = yValueList.get(i);
			// str要 绘制 的 字符串 ,textPaint(TextPaint 类型)设置了字符串格式及属性 的画笔,
			// 之后的参数为设置 画多宽后换行，后面的参数是对齐方式
			StaticLayout layout = new StaticLayout(str, textXYPaint, changeDp(51),
					Alignment.ALIGN_CENTER, 1.0F, 0.0F, true);
			int cur = canvas.save(); // 保存当前状态
			canvas.translate(VIEW_MARGIN + changeDp(7), yAxis);// 画笔的位置
			layout.draw(canvas);
			canvas.restoreToCount(cur);
		}
	}

	/**
	 * 画x轴参考竖线 
	 * @param canvas
	 */
	private void drawXRelativeLineAndText(Canvas canvas) {
		// 画x轴参考竖线
		float degree = (barChartViewBgnRight - barChartViewBgnLeft) / X_HOW_MANY;
		float beginX = barChartViewBgnLeft;

		for (int i = 0; i < X_HOW_MANY; i++) {
			xAxisLineList.add(beginX);
			beginX = beginX + degree;
		}
		xAxisLineList.add(barChartViewBgnRight);// 画最右边的线 (因为float的除法会有误差)

		for (int i = 0; i < xAxisLineList.size(); i++) {
			canvas.drawLine(xAxisLineList.get(i), CHART_TITLE_ROW_HEIHGT + changeDp(28),
					xAxisLineList.get(i), CHART_TITLE_ROW_HEIHGT + changeDp(28) + changeDp(8),
					relativeXLinePaint);
		}
		// 画x轴参考竖线上方显示的值
		for (int i = 0; i < xAxisLineValueList.size(); i++) {
			float xAxis = xAxisLineList.get(i);
			String str = xAxisLineValueList.get(i) + "";
			int fontTotalWidth = changeDp(42);
			StaticLayout layout = new StaticLayout(str, textXYPaint, fontTotalWidth,
					Alignment.ALIGN_CENTER, 1.0F, 0.0F, true);
			float textWidth = textXYPaint.measureText(str);
			float yPosition;// y的位置
			if (textWidth / 2f > fontTotalWidth) {// TODO 这里只做了一行两行的判断处理 [一行6个，数字长度可以到12位]
				yPosition = VIEW_MARGIN + CHART_TITLE_ROW_HEIHGT - changeDp(3);
			} else {
				yPosition = VIEW_MARGIN + CHART_TITLE_ROW_HEIHGT + changeDp(2);
			}
			int cur = canvas.save(); // 保存当前状态
			canvas.translate(xAxis - fontTotalWidth / 2f, yPosition);// 画笔的位置
			layout.draw(canvas);
			canvas.restoreToCount(cur);
		}
	}

	/**
	 * 画x轴的柱状图 
	 * @param canvas
	 */
	private void drawXBarChart(Canvas canvas) {
		switch (positive_Flag) {
		case BOTH_POSITIVE:
			for (int i = 0; i < xValueList.size(); i++) {
				// 背景
				RectF bar = null;
				bar = new RectF(barChartViewBgnLeft, yAxisList.get(i), xAxisList.get(i),
						yAxisList.get(i) + BARCHART_HEIGHT);
				canvas.drawRect(bar, barchartPaint);
				// 文字
				String str = xValueList.get(i) + "";
				StaticLayout layout = new StaticLayout(str, textBarchartPaint,
						(int) textBarchartPaint.measureText(str), Alignment.ALIGN_NORMAL, 1.0F,
						0.0F, true);
				int cur = canvas.save(); // 保存当前状态
				canvas.translate(xAxisList.get(i) + TEXT_PADDING / 2f, yAxisList.get(i));// 画笔的位置
				layout.draw(canvas);
				canvas.restoreToCount(cur);
			}
			break;
		case BOTH_NEGATIVE:
			for (int i = 0; i < xValueList.size(); i++) {
				// 背景
				RectF bar = null;
				bar = new RectF(xAxisList.get(i), yAxisList.get(i), barChartViewBgnRight,
						yAxisList.get(i) + BARCHART_HEIGHT);
				canvas.drawRect(bar, barchartPaint);
				// 文字
				String str = xValueList.get(i) + "";
				float textWidth = textBarchartPaint.measureText(str);
				StaticLayout layout = new StaticLayout(str, textBarchartPaint, (int) textWidth,
						Alignment.ALIGN_OPPOSITE, 1.0F, 0.0F, true);
				int cur = canvas.save(); // 保存当前状态
				canvas.translate(xAxisList.get(i) - textWidth - TEXT_PADDING / 2f, yAxisList.get(i));// 画笔的位置
				layout.draw(canvas);
				canvas.restoreToCount(cur);
			}
			break;
		case POSITIVE_NEGATIVE:
			for (int i = 0; i < xValueList.size(); i++) {
				// 背景
				RectF bar = null;
				if (zeroXAxis < xAxisList.get(i)) {
					bar = new RectF(zeroXAxis, yAxisList.get(i), xAxisList.get(i), yAxisList.get(i)
							+ BARCHART_HEIGHT);
				} else {
					bar = new RectF(xAxisList.get(i), yAxisList.get(i), zeroXAxis,
							yAxisList.get(i) + BARCHART_HEIGHT);
				}
				// bar = new RectF(zeroXAxis, yAxisList.get(i), xAxisList.get(i), yAxisList.get(i)
				// + BARCHART_HEIGHT);
				canvas.drawRect(bar, barchartPaint);
				// 文字
				String str = xValueList.get(i) + "";
				float textWidth = textBarchartPaint.measureText(str);
				if (str.indexOf("-") > -1) {
					StaticLayout layout = new StaticLayout(str, textBarchartPaint, (int) textWidth,
							Alignment.ALIGN_OPPOSITE, 1.0F, 0.0F, true);
					int cur = canvas.save(); // 保存当前状态
					canvas.translate(xAxisList.get(i) - textWidth - TEXT_PADDING / 2f,
							yAxisList.get(i));// 画笔的位置
					layout.draw(canvas);
					canvas.restoreToCount(cur);
				} else {
					StaticLayout layout = new StaticLayout(str, textBarchartPaint, (int) textWidth,
							Alignment.ALIGN_NORMAL, 1.0F, 0.0F, true);
					int cur = canvas.save(); // 保存当前状态
					canvas.translate(xAxisList.get(i) + TEXT_PADDING / 2f, yAxisList.get(i));// 画笔的位置
					layout.draw(canvas);
					canvas.restoreToCount(cur);
				}
			}
			break;
		}
	}

	/**
	 * 画x=0的线 最大值为正数，最小值为负数时
	 * @param canvas
	 */
	private void drawXZeroLineAndText(Canvas canvas) {
		canvas.drawLine(zeroXAxis, CHART_TITLE_ROW_HEIHGT + changeDp(28), zeroXAxis, viewHeight -
				changeDp(13), zeroXPaint);// 画x=0的竖线

		int fontTotalWidth = (int) zeroXTextPaint.measureText("0");
		StaticLayout layout = new StaticLayout("0", zeroXTextPaint, fontTotalWidth,
				Alignment.ALIGN_CENTER, 1.0F, 0.0F, true);
		int cur = canvas.save(); // 保存当前状态
		canvas.translate(zeroXAxis + changeDp(2) , VIEW_MARGIN + CHART_TITLE_ROW_HEIHGT
				+ changeDp(18));// 画笔的位置
		layout.draw(canvas);
		canvas.restoreToCount(cur);
	}

	// ---------------工具方法---------------
	/** dp转化pix像素--工具方法 */
	private int changeDp(int dp) {
		int pix = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
				getResources().getDisplayMetrics()));
		return pix;
	}

	/**
	 * 数字格式化[工具方法]
	 * @param ff 需要处理的数据
	 * @return
	 */
	private String formatNum(float ff) {
		DecimalFormat df;
		String res = "";
		int positiveOrNegativeFlag = 1;// 标识是正数还是负数
		if (ff < 0) {// 先转正数做判断
			positiveOrNegativeFlag = -1;
			ff = -ff;
		}
		if (ff == 0) {
			res = "0";
		} else if (ff < 1) {
			df = new DecimalFormat("###.000");
			res = "0" + df.format(ff) + "";
		} else if (ff > 100) {
			res = (int) ff + "";
		} else {
			df = new DecimalFormat("###.00");
			res = df.format(ff) + "";
		}
		if (positiveOrNegativeFlag == -1) {
			res = "-" + res;
		}
		return res;
	}

	/**
	 * 取需要画的文字的相对y轴的位置 [工具方法]
	 * 
	 * @param paint 画笔
	 * @param targetRect 文字的rect位置
	 * @param type 标识垂直对齐的方式
	 * @return
	 */
	private float getBaseLine(Paint paint, RectF targetRect, int type) {
		float baseline;
		Paint.FontMetricsInt fontMetrics = paint.getFontMetricsInt();
		switch (type) {
		case TEXT_ALIGN_CENTER:// 居中
			baseline = targetRect.top
					+ (targetRect.bottom - targetRect.top - fontMetrics.bottom + fontMetrics.top)
					/ 2 - fontMetrics.top;
			break;
		case TEXT_ALIGN_BOTTOM:// 底边对齐
			baseline = targetRect.top + ((fontMetrics.bottom - fontMetrics.top) / 2);
			break;
		default:
			baseline = 0;
			break;
		}
		return baseline;
	}
	// ===============END===============
}
