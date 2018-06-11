package rs.com.servervrproject.adapter;


import android.databinding.ViewDataBinding;
import android.support.v7.widget.RecyclerView;

import java.util.List;

import rs.com.servervrproject.BR;


/**
 * Created by yasar on 1/1/18.
 */

public abstract class BaseAdapter extends RecyclerView.Adapter<BaseAdapter.MyViewHolder> {

    public List<?> list;

    public BaseAdapter(List<?> t) {
        this.list = t;
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        public final ViewDataBinding binding;

        public MyViewHolder(ViewDataBinding binding) {

            super(binding.getRoot());
            this.binding = binding;

        }

        public void bind(Object obj) {

            binding.setVariable(BR.obj, obj);
            binding.executePendingBindings();

        }
    }
}
