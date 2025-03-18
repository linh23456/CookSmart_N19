package com.example.cooksmart_n19.adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.cooksmart_n19.R;
import com.example.cooksmart_n19.models.Category;

import java.util.List;

public class MainRecycleViewAdapter extends RecyclerView.Adapter<MainRecycleViewAdapter.ViewHolder> {

    private List<Category> categoryList;
    private Context context;

    public MainRecycleViewAdapter(List<Category> categoryList, Context context) {
        this.categoryList = categoryList;
        this.context = context;
    }

    @NonNull
    @Override
    public MainRecycleViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listItem = layoutInflater.inflate(R.layout.recycleview_item_recipe, parent, false);
        return new ViewHolder(listItem, context); // Pass context to ViewHolder
    }

    @Override
    public void onBindViewHolder(@NonNull MainRecycleViewAdapter.ViewHolder holder, int position) {
        Category newCategory = categoryList.get(position);
        holder.txtTitleCategory.setText(newCategory.getTitleCategory());


        RecipeAdapter adapter = new RecipeAdapter(newCategory.getRecipeList(), context);
        holder.viewPager2.setAdapter(adapter);

        holder.setupPageIndicator(newCategory.getRecipeList().size());
        holder.setCurrentIndicator(0);

        holder.viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int pagePosition) {
                super.onPageSelected(pagePosition);
                holder.setCurrentIndicator(pagePosition);
            }
        });
    }

    @Override
    public int getItemCount() {
        return categoryList != null ? categoryList.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView txtTitleCategory;
        private ViewPager2 viewPager2;
        LinearLayout dotsIndicator;
        Context context;

        public ViewHolder(@NonNull View itemView, Context context) {
            super(itemView);
            this.context = context;
            txtTitleCategory = itemView.findViewById(R.id.titleCategory);
            viewPager2 = itemView.findViewById(R.id.viewPager2);
            this.dotsIndicator = itemView.findViewById(R.id.dotsIndicator);
        }

        private void setupPageIndicator(int count) {
            dotsIndicator.removeAllViews();
            ImageView[] dots = new ImageView[count];

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(8, 0, 8, 0);

            for (int i = 0; i < count; i++) {
                dots[i] = new ImageView(context);
                Drawable inactiveDot = ContextCompat.getDrawable(context, R.drawable.inactive_dot);
                dots[i].setImageDrawable(inactiveDot);
                dotsIndicator.addView(dots[i], params);
            }
        }

        private void setCurrentIndicator(int position) {
            int childCount = dotsIndicator.getChildCount();
            for (int i = 0; i < childCount; i++) {
                ImageView imageView = (ImageView) dotsIndicator.getChildAt(i);
                if (i == position) {
                    Drawable activeDot = ContextCompat.getDrawable(context, R.drawable.active_dot);
                    imageView.setImageDrawable(activeDot);
                } else {
                    Drawable inactiveDot = ContextCompat.getDrawable(context, R.drawable.inactive_dot);
                    imageView.setImageDrawable(inactiveDot);
                }
            }
        }
    }
}