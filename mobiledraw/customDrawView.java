package edu.standord.cs108.mobiledraw;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.*;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import java.util.*;


public class customDrawView extends View {

    protected Paint blue_Paint, red_Paint, white_Paint;
    protected static final float SELECTED_STROKE = 15.0f;
    protected static final float REGULAR_STROKE = 5.0f;
    protected float x_right, x_left, y_top, y_bottom;
    protected boolean select_mode, erase_mode, rect_mode, oval_mode;
    protected static ArrayList<GObject> obj_list;
    protected static GObject selected_obj;


    public customDrawView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private static class GObject {
        public float x_cord;
        public float y_cord;
        public float shape_width;
        public float shape_height;
        public boolean is_rect;
        public boolean is_selected;

        public GObject(float x_left, float y_top, float x_right, float y_bottom, boolean is_rect){
            this.is_rect = is_rect;
            this.x_cord=x_left;
            this.y_cord=y_top;
            this.shape_height=y_bottom-y_top;
            this.shape_width=x_right-x_left;
            this.is_selected=true;
        }

        protected void draw_shape(Canvas canvas, Paint stroke, Paint fill){
            if(this.is_rect){
                canvas.drawRect(this.shape(), stroke);
                canvas.drawRect(this.shape(), fill);
            }else{
                canvas.drawOval(this.shape(), stroke);
                canvas.drawOval(this.shape(), fill);
            }
        }

        protected RectF shape(){
            RectF obj_shape = new RectF(x_cord, y_cord, x_cord+shape_width, y_cord+shape_height);
            return obj_shape;
        }
    }



    private void init(){
        blue_Paint = new Paint();
        blue_Paint.setColor(Color.BLUE);//check
        blue_Paint.setStyle(Paint.Style.STROKE);
        blue_Paint.setStrokeWidth(SELECTED_STROKE);
        red_Paint = new Paint();
        red_Paint.setColor(Color.RED);//check
        red_Paint.setStyle(Paint.Style.STROKE);
        red_Paint.setStrokeWidth(REGULAR_STROKE);
        white_Paint = new Paint();
        white_Paint.setColor(Color.rgb(255, 255, 255)); //check
        white_Paint.setStyle(Paint.Style.FILL);
        obj_list = new ArrayList<>();
        selected_obj = null;
        select_mode=false;
        erase_mode=false;
        rect_mode=false;
        oval_mode=false;
    }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            for(int i=0; i<obj_list.size(); i++) {
                GObject to_draw = obj_list.get(i);

                    if (to_draw.is_selected) {
                        to_draw.draw_shape(canvas, blue_Paint, white_Paint);
                    } else {
                        to_draw.draw_shape(canvas, red_Paint, white_Paint);
                }
            }
            }

        @Override
        public boolean onTouchEvent(MotionEvent event){

            RadioGroup rg = (RadioGroup)((Activity) getContext()).findViewById(R.id.modeSelect);
            RadioButton selected_mode = (RadioButton)(rg.findViewById(rg.getCheckedRadioButtonId()));
            oval_mode=false;
            select_mode=false;
            erase_mode=false;
            rect_mode=false;
            if(selected_mode.getText().equals("Oval")){
                oval_mode=true;
            }if(selected_mode.getText().equals("Select")){
                select_mode=true;
            }if(selected_mode.getText().equals("Erase")){
                erase_mode=true;
            }if(selected_mode.getText().equals("Rect")){
                rect_mode=true;
            }

            switch(event.getAction()){
                case MotionEvent.ACTION_DOWN:
                    x_left = event.getX();
                    y_top = event.getY();
                  if(select_mode){
                      unselect_all();
                      findSelectedShape();
                      break;
                  }
                  if(erase_mode){
                         unselect_all();
                         findSelectedShape();
                         remove_shape(selected_obj);
                         break;
                     }

                case MotionEvent.ACTION_UP:
                    x_right = event.getX();
                    y_bottom = event.getY();

                if(x_left>x_right){
                    float temp = x_left;
                    x_left = x_right;
                    x_right = temp;
                }
                    if(y_bottom<y_top){
                        float temp = y_bottom;
                        y_bottom = y_top;
                        y_top = temp;
                    }
                    if(oval_mode || rect_mode) {
                            addShapeList();
                    }
            }
            updateText();
            invalidate();
            return true;
        }

        private void remove_shape(Object to_remove){
            obj_list.remove(to_remove);
            selected_obj=null;
         }

    protected void addShapeList(){
        boolean is_rect;
        if(rect_mode){
            is_rect = true;
        }else{
            is_rect=false;
        }
        GObject to_add = new GObject(x_left, y_top, x_right, y_bottom, is_rect);
        obj_list.add(to_add);
        unselect_all();
        selected_obj=to_add;
        to_add.is_selected=true;
    }

        protected void unselect_all(){
            for(int i=0; i<obj_list.size(); i++){
                obj_list.get(i).is_selected=false;
            }
            selected_obj=null;
        }

        protected void findSelectedShape(){
            for(int i=obj_list.size()-1; i>=0; i--){
                if(obj_list.get(i).shape().contains(x_left,y_top)){
                    obj_list.get(i).is_selected=true;
                    selected_obj=obj_list.get(i);
                    return;
                }
            }

        }

        public void userInputUpdate() {
            EditText x_edit = (EditText) ((Activity) getContext()).findViewById(R.id.x);
            EditText y_edit = (EditText) ((Activity) getContext()).findViewById(R.id.y);
            EditText width_edit = (EditText) ((Activity) getContext()).findViewById(R.id.width);
            EditText height_edit = (EditText) ((Activity) getContext()).findViewById(R.id.height);

            if (selected_obj != null) {
                float input_x = Float.parseFloat(x_edit.getText().toString());
                float input_y = Float.parseFloat(y_edit.getText().toString());
                float input_width = Float.parseFloat(width_edit.getText().toString());
                float input_height = Float.parseFloat(height_edit.getText().toString());
                    selected_obj.x_cord = input_x;
                    selected_obj.y_cord = input_y;
                    selected_obj.shape_width = input_width;
                    selected_obj.shape_height = input_height;

            }
        }

        protected void updateText() {

            EditText x_edit = (EditText) ((Activity) getContext()).findViewById(R.id.x);
            EditText y_edit = (EditText) ((Activity) getContext()).findViewById(R.id.y);
            EditText width_edit = (EditText) ((Activity) getContext()).findViewById(R.id.width);
            EditText height_edit = (EditText) ((Activity) getContext()).findViewById(R.id.height);

           if (selected_obj != null) {

               x_edit.setText(Float.toString(selected_obj.x_cord));
               y_edit.setText(Float.toString(selected_obj.y_cord));
               width_edit.setText(Float.toString(selected_obj.shape_width));
               height_edit.setText(Float.toString(selected_obj.shape_height));
           }else {
                x_edit.setText("");
                y_edit.setText("");
                width_edit.setText("");
                height_edit.setText("");
            }
        }

}
