package dk.au.mad21spring.project.au600963.activities;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import dk.au.mad21spring.project.au600963.R;
import dk.au.mad21spring.project.au600963.model.Recipe;

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder> {
    public interface IRecipeItemClickedListener{
        void onRecipeClicked(int index);
    }

    private IRecipeItemClickedListener listener;
    private List<Recipe> recipeList = new ArrayList<>();

    public RecipeAdapter(IRecipeItemClickedListener listener){
        this.listener = listener;
    }

    //Updating the Recipe list
    public void updateRecipeList(List<Recipe> recipes){
        recipeList = recipes;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);
        RecipeViewHolder vh = new RecipeViewHolder(v, listener);
        return vh;
    }

    //Adding data from arraylist to the recyclerview
    @Override
    public void onBindViewHolder(@NonNull RecipeViewHolder holder, int position) {
        if(recipeList.get(position).getImgUrl() == null){
            Glide.with(holder.imgRecipe.getContext()).load(R.drawable.nodinner).into(holder.imgRecipe);
        } else {
            Glide.with(holder.imgRecipe.getContext()).load(recipeList.get(position).getImgUrl()).into(holder.imgRecipe);
        }

        holder.txtRecipe.setText(recipeList.get(position).getName());
        holder.txtTime.setText("Preparation Time: " + recipeList.get(position).getTime() + " min");
    }

    @Override
    public int getItemCount(){
        return recipeList.size();
    }

    public class RecipeViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        //UI widgets
        ImageView imgRecipe;
        TextView txtRecipe, txtTime;
        IRecipeItemClickedListener listener;

        public RecipeViewHolder(@NonNull View itemView, IRecipeItemClickedListener recipeItemClickedListener) {
            super(itemView);

            imgRecipe = itemView.findViewById(R.id.imgRecipe);
            txtRecipe = itemView.findViewById(R.id.txtRecipe);
            txtTime = itemView.findViewById(R.id.txtTime);
            listener = recipeItemClickedListener;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            listener.onRecipeClicked(getAdapterPosition());
        }
    }
}