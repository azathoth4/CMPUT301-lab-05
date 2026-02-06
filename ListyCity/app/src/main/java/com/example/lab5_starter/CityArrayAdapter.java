package com.example.lab5_starter;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.view.MotionEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.util.ArrayList;

public class CityArrayAdapter extends ArrayAdapter<City> {
    private ArrayList<City> cities;
    private Context context;
    private OnCityDeleteListener deleteListener;

    public interface OnCityDeleteListener {
        void onDeleteCity(City city, int position);
    }

    public CityArrayAdapter(Context context, ArrayList<City> cities, OnCityDeleteListener listener){
        super(context, 0, cities);
        this.cities = cities;
        this.context = context;
        this.deleteListener = listener;
    }

    /**
     * Modified from the answer:
     * Author: Mirek Rusin https://stackoverflow.com/users/177776/mirek-rusin
     * Title: Android: How to handle right to left swipe gestures
     * Answer: https://stackoverflow.com/a/12938787
     * Date: Oct 17, 2012
     * License: CC BY-SA 3.0
     */
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent){
        View view = convertView;
        if (view == null){
            view = LayoutInflater.from(context).inflate(R.layout.layout_city, parent, false);
        }

        final City city = cities.get(position);
        TextView cityName = view.findViewById(R.id.textCityName);
        TextView cityProvince = view.findViewById(R.id.textCityProvince);

        cityName.setText(city.getName());
        cityProvince.setText(city.getProvince());

        view.setTranslationX(0);
        view.setAlpha(1);

        // attach the manual Touch Listener
        view.setOnTouchListener(new View.OnTouchListener() {
            float dX;
            float startX;
            boolean isSwiping = false;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        dX = v.getX() - event.getRawX();
                        startX = event.getRawX();
                        isSwiping = false;
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        float delta = event.getRawX() - startX;

                        // only start swiping if we moved more than a tiny bit
                        if (Math.abs(delta) > 10) {
                            isSwiping = true;
                            parent.requestDisallowInterceptTouchEvent(true);
                        }

                        if (isSwiping && delta > 0) { // Only allow swiping RIGHT
                            v.animate().x(event.getRawX() + dX).setDuration(0).start();
                        }
                        return true;

                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        float finalDelta = event.getRawX() - startX;

                        if (!isSwiping && Math.abs(finalDelta) < 10) {
                            v.performClick(); // this triggers the ListView onItemClick
                            return true;
                        }

                        // check if swiped far enough to delete
                        if (finalDelta > v.getWidth() / 3f) {
                            // animate off screen to the right
                            v.animate()
                                    .translationX(v.getWidth())
                                    .alpha(0)
                                    .setDuration(300)
                                    .setListener(new AnimatorListenerAdapter() {
                                        @Override
                                        public void onAnimationEnd(Animator animation) {
                                            // reset view properties for recycling
                                            v.setTranslationX(0);
                                            v.setAlpha(1);
                                            // trigger Delete
                                            if (deleteListener != null) {
                                                deleteListener.onDeleteCity(city, position);
                                            }
                                        }
                                    }).start();
                        } else {
                            // snap back to original position
                            v.animate().translationX(0).alpha(1).setDuration(200).setListener(null).start();
                        }
                        return true;
                }
                return false;
            }
        });

        return view;
    }
}