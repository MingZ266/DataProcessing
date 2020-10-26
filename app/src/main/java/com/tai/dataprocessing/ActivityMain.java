package com.tai.dataprocessing;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ActivityMain extends AppCompatActivity {
    private Context context = ActivityMain.this;
    private List<Boolean> noSelector = new ArrayList<>();
    private List<BigDecimal> someData = new ArrayList<>();
    private boolean needRestore = false;

    private ImageView editData;
    private ImageView addData;
    private ImageView minusData;
    private EditText inputUb;
    private TextView addTip;
    private TextView minusTip;
    private ListView dataList;
    private Button computer;
    private LinearLayout mainLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Tools.settingStatusBlackWord((AppCompatActivity) context);
        Tools.removeStatusShadow((AppCompatActivity) context);

        initView();
        myListener();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        int[] leftAndTop = new int[2];// [left, top]
        computer.getLocationInWindow(leftAndTop);
        if (ev.getX() >= leftAndTop[0] && ev.getX() <= (leftAndTop[0] + computer.getWidth()/*right*/) &&
                ev.getY() >= leftAndTop[1] && ev.getY() <= (leftAndTop[1] + computer.getHeight()/*bottom*/)) {
            switch (ev.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    needRestore = true;
                    computer.setPadding(fromDpToPx(15), fromDpToPx(5), fromDpToPx(10), 0);
                    break;
                case MotionEvent.ACTION_UP:
                    needRestore = false;
                    computer.setPadding(fromDpToPx(10), 0, fromDpToPx(15), fromDpToPx(5));
                    break;
            }
        } else {
                if (needRestore) {
                    needRestore = false;
                    computer.setPadding(fromDpToPx(10), 0, fromDpToPx(15), fromDpToPx(5));
                    computer.setPressed(false);
                }
        }
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            View focusView = getCurrentFocus();
            if (focusView instanceof EditText) {
                focusView.getLocationInWindow(leftAndTop);
                if (ev.getX() < leftAndTop[0] || ev.getX() > (leftAndTop[0] + focusView.getWidth()) ||
                        ev.getY() < leftAndTop[1] || ev.getY() > (leftAndTop[1] + focusView.getHeight())) {
                    mainLayout.setFocusableInTouchMode(false);
                    ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(/*getWindow().getDecorView()*/mainLayout.getWindowToken(), 0);
                    mainLayout.setFocusableInTouchMode(true);
                    mainLayout.requestFocus();
                    return true;
                }
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    private void initView() {
        editData = findViewById(R.id.editData);
        addData = findViewById(R.id.addData);
        minusData = findViewById(R.id.minusData);
        inputUb = findViewById(R.id.inputUb);
        addTip = findViewById(R.id.addTip);
        minusTip = findViewById(R.id.minusTip);
        dataList = findViewById(R.id.dataList);
        computer = findViewById(R.id.compute);
        mainLayout = findViewById(R.id.mainLayout);
    }

    private void myListener() {
        mainLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });

        editData.setOnClickListener(new View.OnClickListener() {
            private int index;

            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View v) {
                for (int i = 0; i < noSelector.size(); i++) {
                    if (!noSelector.get(i)) {
                        index = i;
                        final View view = View.inflate(context, R.layout.dialog_edit_data, null);
                        ((TextView) view.findViewById(R.id.data)).setText(someData.get(index).toString());
                        final AlertDialog dialog = new AlertDialog.Builder(context, R.style.AlertDialogCornerRadius)
                                .setView(view)
                                .create();
                        dialog.show();
                        dialog.setCanceledOnTouchOutside(false);

                        view.findViewById(R.id.OK).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String dataChangeStr = String.valueOf(((EditText) view.findViewById(R.id.dataChange)).getText());
                                if (dataChangeStr.equals(""))
                                    Toast.makeText(context, "请输入改变后的数据", Toast.LENGTH_SHORT).show();
                                else {
                                    dialog.cancel();
                                    someData.set(index, new BigDecimal(dataChangeStr));
                                    noSelector.set(index, true);
                                    editData.setVisibility(View.GONE);
                                    minusTip.setVisibility(View.GONE);
                                    refreshDataList();
                                }
                            }
                        });

                        view.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.cancel();
                            }
                        });
                        return;
                    }
                }
            }
        });

        addData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (boolean theItem : noSelector) {
                    if (!theItem)
                        return;
                }
                if (someData.size() < 10) {
                    final View view = View.inflate(context, R.layout.dialog_add_data, null);
                    final AlertDialog dialog = new AlertDialog.Builder(context, R.style.AlertDialogCornerRadius)
                            .setView(view)
                            .create();
                    dialog.show();
                    dialog.setCanceledOnTouchOutside(false);

                    view.findViewById(R.id.OK).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String inputData = String.valueOf(((EditText) view.findViewById(R.id.data)).getText());
                            if (inputData.equals(""))
                                Toast.makeText(context, "请输入数据", Toast.LENGTH_SHORT).show();
                            else {
                                dialog.cancel();
                                if (someData.size() == 0) {
                                    addTip.setVisibility(View.GONE);
                                    computer.setVisibility(View.VISIBLE);
                                }
                                someData.add(new BigDecimal(inputData));
                                noSelector.add(true);
                                refreshDataList();
                            }
                        }
                    });

                    view.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.cancel();
                        }
                    });
                } else
                    Toast.makeText(context, "最多添加10个数据", Toast.LENGTH_SHORT).show();
            }
        });

        minusData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (someData.size() == 0)
                    Toast.makeText(context, "没有数据", Toast.LENGTH_SHORT).show();
                else {
                    StringBuffer s = new StringBuffer();
                    final List<Integer> indexList = new ArrayList<>();
                    for (int i = 0; i < noSelector.size(); i++) {
                        if (!noSelector.get(i)) {
                            if (indexList.size() > 0)
                                s.append('\n');
                            s.append(someData.get(i));
                            indexList.add(i);
                        }
                    }
                    if (indexList.size() == 0)
                        Toast.makeText(context, "请选择欲删除的数据", Toast.LENGTH_SHORT).show();
                    else {
                        View view = View.inflate(context, R.layout.dialog_minus_data, null);
                        ((TextView) view.findViewById(R.id.data)).setText(s);
                        final AlertDialog dialog = new AlertDialog.Builder(context, R.style.AlertDialogCornerRadius)
                                .setView(view)
                                .create();
                        dialog.show();
                        dialog.setCanceledOnTouchOutside(false);

                        view.findViewById(R.id.OK).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.cancel();
                                if (indexList.size() == 1)
                                    editData.setVisibility(View.GONE);
                                for (int i = indexList.size() - 1; i >= 0; i--) {
                                    someData.remove((int) indexList.get(i));
                                    noSelector.remove((int) indexList.get(i));
                                }
                                minusTip.setVisibility(View.GONE);
                                if (someData.size() == 0) {
                                    addTip.setVisibility(View.VISIBLE);
                                    computer.setVisibility(View.GONE);
                                }
                                refreshDataList();
                            }
                        });

                        view.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.cancel();
                            }
                        });
                    }
                }
            }
        });

        dataList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ViewHolder viewHolder = (ViewHolder) view.getTag();
                if (noSelector.get(position)) {
                    noSelector.set(position, false);
                    viewHolder.outline.setEnabled(false);
                    viewHolder.num.setEnabled(false);
                    viewHolder.data.setEnabled(false);
                } else {
                    noSelector.set(position, true);
                    viewHolder.outline.setEnabled(true);
                    viewHolder.num.setEnabled(true);
                    viewHolder.data.setEnabled(true);
                }
                if (noSelector.indexOf(false) < 0)
                    minusTip.setVisibility(View.GONE);
                else
                    minusTip.setVisibility(View.VISIBLE);
                int count = 0;
                for (boolean theItem : noSelector) {
                    if (!theItem)
                        count++;
                }
                if (count == 1)
                    editData.setVisibility(View.VISIBLE);
                else
                    editData.setVisibility(View.GONE);
            }
        });

        computer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (boolean theItem : noSelector) {
                    if (!theItem)
                        return;
                }
                String ubStr = String.valueOf(inputUb.getText());
                if (ubStr.equals(""))
                    Toast.makeText(context, "请输入B类不确定度", Toast.LENGTH_SHORT).show();
                else {
                    Compute compute = new Compute(someData, new BigDecimal(ubStr));
                    View view = View.inflate(context, R.layout.dialog_display_result, null);
                    ((TextView) view.findViewById(R.id.result)).setText(compute.getResult());
                    final AlertDialog dialog = new AlertDialog.Builder(context, R.style.AlertDialogCornerRadius)
                            .setView(view)
                            .create();
                    dialog.show();
                    dialog.setCanceledOnTouchOutside(false);

                    view.findViewById(R.id.OK).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.cancel();
                        }
                    });
                }
            }
        });
    }

    private int fromDpToPx(float dpValue) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpValue, getResources().getDisplayMetrics());
    }

    private void refreshDataList() {
        dataList.setAdapter(new DataListAdapter());
    }

    private class DataListAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return someData.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @SuppressLint("SetTextI18n")
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if (convertView == null) {
                convertView = View.inflate(context, R.layout.list_item_data_list, null);
                viewHolder = new ViewHolder();
                viewHolder.outline = convertView.findViewById(R.id.outline);
                viewHolder.num = convertView.findViewById(R.id.num);
                viewHolder.data = convertView.findViewById(R.id.data);
                convertView.setTag(viewHolder);
            } else
                viewHolder = (ViewHolder) convertView.getTag();
            viewHolder.num.setText("数据 " + (position + 1));
            viewHolder.data.setText(String.valueOf(someData.get(position)));
            return convertView;
        }
    }

    private static class ViewHolder {
        LinearLayout outline;
        TextView num;
        TextView data;
    }
}