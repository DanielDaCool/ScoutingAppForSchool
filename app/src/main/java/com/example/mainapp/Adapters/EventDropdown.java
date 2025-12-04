package com.example.mainapp.Adapters;

import android.animation.ValueAnimator;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.example.mainapp.R;
import com.example.mainapp.TBAHelpers.EVENTS;

public class EventDropdown {

    private Context context;
    private Button dropdownButton;
    private LinearLayout dropdownMenu;
    private TextView dropdownArrow;
    private boolean isExpanded = false;
    private EVENTS selectedEvent;
    private OnEventSelectedListener listener;

    // Callback interface
    public interface OnEventSelectedListener {
        void onEventSelected(EVENTS event);
    }

    public EventDropdown(Context context, View rootView) {
        this.context = context;
        this.dropdownButton = rootView.findViewById(R.id.dropdownButton);
        this.dropdownMenu = rootView.findViewById(R.id.dropdownMenu);
        this.dropdownArrow = rootView.findViewById(R.id.dropdownArrow);

        initialize();
    }

    private void initialize() {
        // Set default selection
        EVENTS[] events = EVENTS.values();
        if (events.length > 0) {
            selectedEvent = events[0];
            dropdownButton.setText(selectedEvent.toString());
        }

        // Setup click listener
        dropdownButton.setOnClickListener(v -> toggleDropdown());

        // Build menu items
        buildDropdownMenu();
    }

    private void buildDropdownMenu() {
        dropdownMenu.removeAllViews();

        // Add category: Israel Events
        addCategoryHeader("🇮🇱 Israel Events");
        addMenuItem(EVENTS.DISTRICT_1);
        addMenuItem(EVENTS.DISTRICT_2);
        addMenuItem(EVENTS.DISTRICT_3);
        addMenuItem(EVENTS.DISTRICT_4);
        addMenuItem(EVENTS.DCMP);

        addDivider();

//        // Add category: Houston Championship
//        addCategoryHeader("🌎 Houston Championship");
//        addMenuItem(EVENTS.ARCHIMEDES);
//        addMenuItem(EVENTS.CURIE);
//        addMenuItem(EVENTS.DALY);
//        addMenuItem(EVENTS.GALILEO);
//        addMenuItem(EVENTS.HOPPER);
//        addMenuItem(EVENTS.JOHNSON);
//        addMenuItem(EVENTS.MILSTEIN);
//        addMenuItem(EVENTS.NEWTON);
//
//        addDivider();

//        addCategoryHeader("⭐ Finals");
//        addMenuItem(EVENTS.EINSTEIN);
    }

    private void addCategoryHeader(String title) {
        TextView header = new TextView(context);
        header.setText(title);
        header.setTextSize(12);
        header.setTextColor(ContextCompat.getColor(context, R.color.secondary_text));
        header.setTypeface(null, android.graphics.Typeface.BOLD);
        header.setPadding(16, 12, 16, 8);
        dropdownMenu.addView(header);
    }

    private void addMenuItem(EVENTS event) {
        TextView item = new TextView(context);
        item.setText(event.toString());
        item.setTextSize(16);
        item.setTextColor(ContextCompat.getColor(context, R.color.primary_text));
        item.setPadding(16, 20, 16, 20);
        item.setBackground(ContextCompat.getDrawable(context, R.drawable.dropdown_item_background));

        // Highlight selected item
        if (event == selectedEvent) {
            item.setTextColor(ContextCompat.getColor(context, R.color.button_text));
            item.setTypeface(null, android.graphics.Typeface.BOLD);
        }

        item.setOnClickListener(v -> {
            selectedEvent = event;
            dropdownButton.setText(event.toString());
            collapseDropdown();

            // Notify listener
            if (listener != null) {
                listener.onEventSelected(event);
            }

            // Rebuild menu to update highlighting
            buildDropdownMenu();
        });

        dropdownMenu.addView(item);
    }

    private void addDivider() {
        View divider = new View(context);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                1
        );
        params.setMargins(0, 8, 0, 8);
        divider.setLayoutParams(params);
        divider.setBackgroundColor(ContextCompat.getColor(context, R.color.button_border));
        dropdownMenu.addView(divider);
    }

    private void toggleDropdown() {
        if (isExpanded) {
            collapseDropdown();
        } else {
            expandDropdown();
        }
    }

    private void expandDropdown() {
        dropdownMenu.setVisibility(View.VISIBLE);

        // Measure the menu height
        dropdownMenu.measure(
                View.MeasureSpec.makeMeasureSpec(dropdownButton.getWidth(), View.MeasureSpec.EXACTLY),
                View.MeasureSpec.UNSPECIFIED
        );
        final int targetHeight = dropdownMenu.getMeasuredHeight();

        // Animate expansion
        dropdownMenu.getLayoutParams().height = 0;
        ValueAnimator animator = ValueAnimator.ofInt(0, targetHeight);
        animator.setDuration(300);
        animator.addUpdateListener(animation -> {
            ViewGroup.LayoutParams params = dropdownMenu.getLayoutParams();
            params.height = (int) animation.getAnimatedValue();
            dropdownMenu.setLayoutParams(params);
        });
        animator.start();

        // Rotate arrow
        dropdownArrow.animate().rotation(180).setDuration(300).start();

        isExpanded = true;
    }

    private void collapseDropdown() {
        final int startHeight = dropdownMenu.getHeight();

        // Animate collapse
        ValueAnimator animator = ValueAnimator.ofInt(startHeight, 0);
        animator.setDuration(300);
        animator.addUpdateListener(animation -> {
            ViewGroup.LayoutParams params = dropdownMenu.getLayoutParams();
            params.height = (int) animation.getAnimatedValue();
            dropdownMenu.setLayoutParams(params);

            if ((int) animation.getAnimatedValue() == 0) {
                dropdownMenu.setVisibility(View.GONE);
            }
        });
        animator.start();

        // Rotate arrow back
        dropdownArrow.animate().rotation(0).setDuration(300).start();

        isExpanded = false;
    }

    // Public methods
    public EVENTS getSelectedEvent() {
        return selectedEvent;
    }

    public void setSelectedEvent(EVENTS event) {
        this.selectedEvent = event;
        dropdownButton.setText(event.toString());
        buildDropdownMenu();
    }

    public void setOnEventSelectedListener(OnEventSelectedListener listener) {
        this.listener = listener;
    }
}