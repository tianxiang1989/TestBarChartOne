package com.test.testbarchartone;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.RelativeLayout;

/**
 * 柱状图
 * liuxiuquan
 * 2014-9-19
 */
public class MainActivity extends Activity {
	/** 打印log信息时的标识 */
	private final static String TAG = MainActivity.class.getSimpleName();

	private BarChartView v1;
	private RelativeLayout rl_barchart_out;
	private Context context;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		context = MainActivity.this;
		setContentView(R.layout.activity_main);
		// v1 = (BarChartView) findViewById(R.id.view);
		// v1.setLayoutParams(params)
		rl_barchart_out = (RelativeLayout) findViewById(R.id.ll_barchart_out);
		DrawThread th = new DrawThread();
		th.start();
	}

	// 模拟请求网络的延时thread
	public class DrawThread extends Thread {
		@Override
		public void run() {
			try {
				Thread.sleep(1500);
				MainActivity.this.runOnUiThread(updateThread);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		/**
		 * 增加自定义chart的thread
		 */
		Runnable updateThread = new Runnable() {
			@Override
			public void run() {
				// Toast.makeText(context, "viewWidth==", Toast.LENGTH_SHORT).show();

				// 数据源分析：
				// {"measurename":"日收入", //上方的标题
				// "value":"3946", //上方标题的值
				// "ringrate":"--", //环比
				// "unit":"万元", //单位
				// "column":{"y":["689","568","465","457","368","299","296","231","227","182","165"],//柱状图y数据
				// "x":["保定","石家庄","唐山","邯郸","沧州","邢台","廊坊","张家口","承德","秦皇岛","衡水"]},//柱状图x数据
				// "regionname":"河北省", //地域
				// "cycle":"日", //标识是日数据还是月数据,需要根据这个处理date,再赋值给折线图
				// "date":"2014年6月19日 ",//日期
				// "measuerid":"DAY_INCOME",//指标名称，未用到，和kpiId相同
				// "state":"语音、短信、彩信、梦网、GPRS话单日收入" //口径
				// ,"samerate":"-13.50%", //同比
				// "source":"市场运营简报", //来源
				// "zhexian":{"y":["4040","3721","3457","4238","3912","3946"],//折线图y数据
				// "x":["6月14日 ","6月15日 ","6月16日 ","6月17日 ","6月18日 ","6月19日 "]}} //折线图x数据

				// 1 测试数据 最大最小均为正值
				/*String strJson = "{\"measurename\":\"日收入\",\"value\":\"3946\",\"ringrate\":\"--\",\"unit\":\"万元\",\"column\":"
						+ "{\"y\":[\"689\",\"568\",\"465\",\"457\",\"368\",\"299\",\"296\",\"231\",\"157\",\"87\",\"33\""
						+ "],\"x\":[\"保定五六七八九\",\"石家庄一二三四五\",\"唐山\",\"邯郸\",\"沧州\",\"邢台\",\"廊坊\",\"张家口\",\"承德\",\"秦皇岛\",\"衡水\"]},"
						+ "\"regionname\":\"河北省\",\"cycle\":\"日\",\"date\":\"2014年6月19日 \",\"measuerid\":\"DAY_INCOME\""
						+ ",\"state\":\"语音、短信、彩信、梦网、GPRS话单日收入\",\"samerate\":\"-13.50%\",\"source\":\"市场运营简报\""
						+ ",\"zhexian\":{\"y\":[\"4040\",\"3721\",\"3457\",\"4238\",\"3912\",\"3946\"],\"x\":[\"6月14日 \""
						+ ",\"6月15日 \",\"6月16日 \",\"6月17日 \",\"6月18日 \",\"6月19日 \"]}}";*/

				// 2 测试数据 最大为正值 最小为负值
				String strJson = "{\"measurename\":\"日收入\",\"value\":\"3946\",\"ringrate\":\"--\",\"unit\":\"万元\",\"column\":"
						+ "{\"y\":[\"689\",\"568\",\"465\",\"457\",\"368\",\"299\",\"125\",\"-81\",\"-157\",\"-287\",\"-610\""
						+ "],\"x\":[\"保定五六七八九\",\"石家庄一二三四五\",\"唐山\",\"邯郸\",\"沧州\",\"邢台\",\"廊坊\",\"张家口\",\"承德\",\"秦皇岛\",\"衡水\"]},"
						+ "\"regionname\":\"河北省\",\"cycle\":\"日\",\"date\":\"2014年6月19日 \",\"measuerid\":\"DAY_INCOME\""
						+ ",\"state\":\"语音、短信、彩信、梦网、GPRS话单日收入\",\"samerate\":\"-13.50%\",\"source\":\"市场运营简报\""
						+ ",\"zhexian\":{\"y\":[\"4040\",\"3721\",\"3457\",\"4238\",\"3912\",\"3946\"],\"x\":[\"6月14日 \""
						+ ",\"6月15日 \",\"6月16日 \",\"6月17日 \",\"6月18日 \",\"6月19日 \"]}}";

				// 3 测试数据 最大值和 最小均为负值
				/*String strJson = "{\"measurename\":\"日收入\",\"value\":\"3946\",\"ringrate\":\"--\",\"unit\":\"万元\",\"column\":"
						+ "{\"y\":[\"-4\",\"-81\",\"-165\",\"-257\",\"-290\",\"-327\",\"-368\",\"-454\",\"-512\",\"-666\",\"-773\""
						+ "],\"x\":[\"保定五六七八九\",\"石家庄一二三四五\",\"唐山\",\"邯郸\",\"沧州\",\"邢台\",\"廊坊\",\"张家口\",\"承德\",\"秦皇岛\",\"衡水\"]},"
						+ "\"regionname\":\"河北省\",\"cycle\":\"日\",\"date\":\"2014年6月19日 \",\"measuerid\":\"DAY_INCOME\""
						+ ",\"state\":\"语音、短信、彩信、梦网、GPRS话单日收入\",\"samerate\":\"-13.50%\",\"source\":\"市场运营简报\""
						+ ",\"zhexian\":{\"y\":[\"4040\",\"3721\",\"3457\",\"4238\",\"3912\",\"3946\"],\"x\":[\"6月14日 \""
						+ ",\"6月15日 \",\"6月16日 \",\"6月17日 \",\"6月18日 \",\"6月19日 \"]}}";*/

				String measurename = getOnedata(strJson, "measurename");
				String value = getOnedata(strJson, "value");
				String ringrate = getOnedata(strJson, "ringrate");
				String unit = getOnedata(strJson, "unit");
				String regionname = getOnedata(strJson, "regionname");
				String cycle = getOnedata(strJson, "cycle");
				String date = getOnedata(strJson, "date");
				String state = getOnedata(strJson, "state");
				String samerate = getOnedata(strJson, "samerate");
				String source = getOnedata(strJson, "source");
				String column = getOnedata(strJson, "column");

				String chartTitleName = date + regionname + "地域分布(单位:" + unit + ")";

				JSONObject columnValues;
				try {
					columnValues = new JSONObject(column);
					JSONArray columnXArray = columnValues.getJSONArray("y");
					JSONArray columnYArray = columnValues.getJSONArray("x");
					List<Float> columnXList = new ArrayList<Float>();// x轴数据
					List<String> columnYList = new ArrayList<String>();// y轴数据
					for (int i = 0; i < columnXArray.length(); i++) {
						columnXList.add(Float.parseFloat(columnXArray.getString(i)));
						columnYList.add(columnYArray.getString(i));
					}
					int sizeY = columnYList.size();
					v1 = new BarChartView(context, sizeY);
					v1.setChartTitleName(chartTitleName);// 设置表报标题
					v1.setxValueList(columnXList);
					v1.setyValueList(columnYList);
					v1.startRunDraw();
					Log.v(TAG, "v1.startRunDraw()");

					rl_barchart_out.addView(v1);// 将自定义view加到relativeLayout中
					// 改变元素
					// ll_barchart_out.removeView(v1);
					// v1=new BarChartView(this, 3);
					// ll_barchart_out.addView(v1);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		};

		/**
		 * 根据key获取json中的value
		 * @param json
		 * @param which
		 * @return
		 * @throws JSONException
		 */
		public String getOnedata(String json, String which) {
			String result = "";
			JSONObject jsonObject;
			try {
				jsonObject = new JSONObject(json);
				result = jsonObject.getString(which).toString();
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return result;
		}
	}
}
