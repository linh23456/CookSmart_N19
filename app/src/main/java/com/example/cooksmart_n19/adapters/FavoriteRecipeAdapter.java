package com.example.cooksmart_n19.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cooksmart_n19.R;
import com.example.cooksmart_n19.fragments.FavoriteFragment;
import com.example.cooksmart_n19.models.Recipe;

import java.util.List;

public class FavoriteRecipeAdapter extends RecyclerView.Adapter<FavoriteRecipeAdapter.RecipeViewHolder> {
    private List<Recipe> recipes;
    private final OnItemClickListener onLikeClickListener;
    private final OnItemClickListener onDetailClickListener;
    private final FavoriteFragment favoriteFragment;//Được sử dụng để kiểm tra xem một công thức đã được thích chưa

    public interface OnItemClickListener {
        void onItemClick(Recipe recipe, int position);
    }

    public FavoriteRecipeAdapter(List<Recipe> recipes, OnItemClickListener onLikeClickListener, OnItemClickListener onDetailClickListener) {
        this.recipes = recipes;
        //this.recipes: là biến thành viên (thuộc về class FavoriteRecipeAdapter).
        //recipes: là đối số được truyền vào constructor.
        //➡️ Câu này có nghĩa: "gán danh sách công thức recipes từ ngoài vào biến recipes của adapter này để hiển thị trong RecyclerView."
        this.onLikeClickListener = onLikeClickListener;
        //onLikeClickListener: là một interface listener để xử lý khi người dùng bấm vào nút "thích" (heart icon).
        //Gán nó vào biến thành viên để có thể dùng về sau trong onBindViewHolder.
        //➡️ Câu này có nghĩa: "khi user click vào icon trái tim, adapter biết phải gọi đến callback nào."
        this.onDetailClickListener = onDetailClickListener;
        //Tương tự như trên, nhưng dùng cho nút xem chi tiết công thức.
        //➡️ Câu này giúp adapter xử lý hành động khi user bấm vào nút “Xem chi tiết”.
        this.favoriteFragment = null;
        //Adapter không cần kiểm tra trạng thái "thích" trực tiếp từ FavoriteFragment.
        //Chỉ cần dùng cho RecyclerView đơn giản hiển thị danh sách mà không quan tâm biểu tượng "thích" có bật hay không.
        //🧠 Lưu ý:
        //favoriteFragment được gán bằng null, vì không cần truy cập các hàm hoặc dữ liệu từ Fragment cha.
    }

    public FavoriteRecipeAdapter(List<Recipe> recipes, OnItemClickListener onLikeClickListener, OnItemClickListener onDetailClickListener, FavoriteFragment favoriteFragment) {
        this.recipes = recipes;
        this.onLikeClickListener = onLikeClickListener;
        this.onDetailClickListener = onDetailClickListener;
        this.favoriteFragment = favoriteFragment;
        //favoriteFragment là một đối tượng Fragment đại diện cho màn hình yêu thích.
        //Mục tiêu là hiển thị biểu tượng trái tim đúng với trạng thái thích hiện tại từ Fragment.
        //🧠 Lưu ý:
        //Phù hợp dùng trong FavoriteFragment – nơi lưu trữ danh sách các công thức đã thích.
    }

    @NonNull
    @Override
    public RecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recipe, parent, false);
        return new RecipeViewHolder(view);
        //. Nó được gọi khi RecyclerView cần tạo mới một ViewHolder để hiển thị một item.
        //Tạo ra layout cho mỗi item trong danh sách,
        //Bọc nó lại trong một ViewHolder,
        //Và trả về để RecyclerView dùng hiển thị dữ liệu.
    }

    @Override
    //Phương thức onBindViewHolder() là nơi gán dữ liệu cho từng item trong RecyclerView.
    // Nó được gọi mỗi khi một item sắp hiển thị lên màn hình.
    public void onBindViewHolder(@NonNull RecipeViewHolder holder, int position) {
        Recipe recipe = recipes.get(position);
        holder.textViewTitle.setText(recipe.getTitle());
        holder.textViewDifficulty.setText("Độ khó: " + recipe.getDifficulty());
        holder.textViewCookingTime.setText("Thời gian: " + recipe.getCookingTime() + " phút");
        holder.textViewCost.setText("Chi phí: " + recipe.getCost() + " VNĐ");

        // Cập nhật trạng thái nút "Thích"
        if (favoriteFragment != null) {
            boolean isLiked = favoriteFragment.isRecipeLiked(recipe.getRecipeId());
            holder.buttonLike.setImageResource(isLiked ? R.drawable.ic_heart_filled : R.drawable.ic_heart_outline);
        }
        //Nếu favoriteFragment tồn tại (được truyền từ bên ngoài):
        //
        //Kiểm tra xem món ăn này có được "thích" bởi người dùng không (isRecipeLiked()).
        //
        //Nếu được thích → hiển thị icon trái tim đầy (ic_heart_filled).
        //
        //Nếu không thích → hiển thị trái tim rỗng (ic_heart_outline).

        holder.buttonLike.setOnClickListener(v -> onLikeClickListener.onItemClick(recipe, position));
        holder.buttonDetail.setOnClickListener(v -> onDetailClickListener.onItemClick(recipe, position));
        //Khi người dùng bấm vào nút buttonLike, sẽ gọi onLikeClickListener.
        //
        //Khi bấm vào nút buttonDetail, sẽ gọi onDetailClickListener.
        //
        //Cả hai listener này được truyền vào từ Fragment hoặc Activity để xử lý logic (ví dụ: thích/bỏ thích, mở chi tiết món ăn…).
    }

    @Override
    public int getItemCount() {
        return recipes.size();
    }

    public void updateRecipes(List<Recipe> newRecipes) {
        this.recipes = newRecipes;
        notifyDataSetChanged();
    }


    static class RecipeViewHolder extends RecyclerView.ViewHolder {
        TextView textViewTitle;
        TextView textViewDifficulty;
        TextView textViewCookingTime;
        TextView textViewCost;
        ImageButton buttonLike;
        Button buttonDetail;

        public RecipeViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewTitle = itemView.findViewById(R.id.textViewTitle);
            textViewDifficulty = itemView.findViewById(R.id.textViewDifficulty);
            textViewCookingTime = itemView.findViewById(R.id.textViewCookingTime);
            textViewCost = itemView.findViewById(R.id.textViewCost);
            buttonLike = itemView.findViewById(R.id.buttonLike);
            buttonDetail = itemView.findViewById(R.id.buttonViewDetail);
        }
    }
}