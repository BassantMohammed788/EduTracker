import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.edutracker.R
import com.example.edutracker.databinding.GradeLevelItemBinding

class GradeLevelAdapter(private var GradeLevelsList: List<String>,private var clickListener: (String) -> Unit) :
    RecyclerView.Adapter<GradeLevelAdapter.GradeLevelViewHolder>() {
    private lateinit var binding: GradeLevelItemBinding
lateinit var context:Context
    inner class GradeLevelViewHolder(var binding: GradeLevelItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GradeLevelViewHolder {
        val inflater: LayoutInflater =
            parent.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        context=parent.context
        binding = GradeLevelItemBinding.inflate(inflater, parent, false)
        return GradeLevelViewHolder(binding)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(holder: GradeLevelViewHolder, position: Int) {
        val currentLevel = GradeLevelsList[position]
        holder.binding.GradeName.text = currentLevel

        val hoverBackgroundDrawable = ContextCompat.getDrawable(context, R.drawable.selector_hover_background)

        holder.binding.currencyItem.setOnClickListener {
            holder.binding.constraint.background = hoverBackgroundDrawable
            clickListener(currentLevel)
        }

    }

    override fun getItemCount(): Int {
        return GradeLevelsList.size
    }

    fun setGradeLevelsList(currencyList: List<String>) {
        this.GradeLevelsList = currencyList
        notifyDataSetChanged()
    }
}