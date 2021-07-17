package munna.hometech.quizlearn;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class BookmarkAdapter extends RecyclerView.Adapter<BookmarkAdapter.ViewHolder> {

    private List<QuestionModel> questionModelList;

    public BookmarkAdapter(List<QuestionModel> questionModelList) {
        this.questionModelList = questionModelList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.bookmark_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.setData(questionModelList.get(position).getQuestion(),questionModelList.get(position).getCorrectANS(),position);
    }

    @Override
    public int getItemCount() {
        return questionModelList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tvQuestion, tvAnswer;
        private ImageButton deleteBookmark;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvQuestion = itemView.findViewById(R.id.tv_question);
            tvAnswer = itemView.findViewById(R.id.tv_answer);
            deleteBookmark = itemView.findViewById(R.id.delete_bookmark);
        }

        private void setData(String question, String answer, int position) {
            tvQuestion.setText(question);
            tvAnswer.setText(answer);

            deleteBookmark.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    questionModelList.remove(position);
                    notifyItemRemoved(position);
                }
            });
        }
    }
}
