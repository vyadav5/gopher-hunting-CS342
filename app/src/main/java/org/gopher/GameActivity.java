package org.gopher;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import static java.lang.Math.abs;
import static java.lang.Math.random;

public class GameActivity extends AppCompatActivity {

    abstract class Player extends Thread {

        public Handler handler;
        int player;

        Player(int player) { this.player = player; }

        @Override
        public void run() {

            Looper.prepare();
            handler = new Handler(Looper.myLooper());
            Looper.loop();
        }

        abstract int strategy();

        public void play() {

            int delay;
            if (mode.equals("Guess")) delay = 0;
            else delay = 1000;

            handler.postDelayed(() -> {

                Message msg = new Message();
                msg.arg1 = strategy();
                msg.arg2 = player;

                GameActivity.this.handler.sendMessage(msg);

            }, delay);
        }
    }

    class ST1 extends Player {

        ST1(int player) { super(player); }

        @Override
        int strategy() { return (int)(random() * 100); }
    }

    class ST2 extends Player {

        int current = -3;

        ST2(int player) { super(player); }

        @Override
        int strategy() { return current = (current + 3) % 100; }
    }

    class Adapter extends RecyclerView.Adapter<Adapter.Holder> {

        class Holder extends RecyclerView.ViewHolder {

            public ImageView view;
            public Holder(@NonNull ImageView itemView) { super(itemView); view = itemView; }
        }

        @NonNull
        public Holder onCreateViewHolder(@NonNull ViewGroup parent, int type) {

            ImageView view = new ImageView(parent.getContext());

            RecyclerView.LayoutParams params = new RecyclerView.LayoutParams(64, 64);
            params.setMargins(12, 12, 12, 12);

            view.setLayoutParams(params);

            return new Holder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull Holder holder, int position) {

            int color = Color.BLACK;
            if (board[position] == 1) color = Color.BLUE;
            else if (board[position] == 2) color = Color.RED;
            else if (board[position] == 3) color = Color.CYAN;
            else if (board[position] == 4) color = Color.parseColor("#dea712");

            holder.view.setBackgroundColor(color);
        }

        @Override
        public int getItemCount() { return 100; }
    }

    RecyclerView recycler;
    int[] board = new int[100];

    Player[] players = new Player[2];

    Adapter adapter;

    String mode = "Guess";
    int gopher = (int)(random() * 100);

    TextView status;

    Handler handler;
    int turn = 0;

    ArrayList<Integer> near = new ArrayList<>();
    ArrayList<Integer> far = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        mode = getIntent().getStringExtra("Mode");
        proximity();

        board[gopher] = 4;

        recycler = findViewById(R.id.Recycler);
        recycler.setLayoutManager(new GridLayoutManager(this, 10));
        recycler.setAdapter(adapter = new Adapter());

        status = findViewById(R.id.Status);

        handler = new Handler(Looper.getMainLooper()) {

            @Override
            public void handleMessage(@NonNull Message msg) {

                int position = msg.arg1;
                int player = msg.arg2;

                String message = "";

                int distance = abs(position - gopher);

                if (position == gopher) message = "Success, player " + player + " wins!";

                else {

                    message = "Player " + player + " - ";

                    if (near.contains(position)) message += "Near miss";
                    else if (far.contains(position)) message += "Close guess";
                    else message += "Complete miss";

                    if (board[position] != 0) message += " - Disaster";
                }

                status.setText(message);

                if (board[position] == 0 || board[position] == 4) board[position] = player;
                else if (board[position] == (player % 2) + 1) board[position] = 3;

                adapter.notifyDataSetChanged();

                turn = (turn + 1) % 2;

                if (position == gopher) {

                    findViewById(R.id.Next).setVisibility(View.INVISIBLE);
                    findViewById(R.id.Continuous).setVisibility(View.INVISIBLE);
                }

                else {

                    if (mode.equals("Continuous")) players[turn].play();
                    else ((Button)findViewById(R.id.Next)).setText("NEXT TURN (PLAYER " + (turn + 1) + ")");
                }
            }
        };

        players[0] = new ST1(1);
        players[1] = new ST2(2);

        players[0].start();
        players[1].start();

        findViewById(R.id.Next).setOnClickListener((View v) -> players[turn].play());

        findViewById(R.id.Continuous).setOnClickListener((View v) -> {

            mode = "Continuous";
            continuous();
        });

        if (mode.equals("Continuous")) continuous();
    }

    void continuous() {

        findViewById(R.id.Next).setVisibility(View.INVISIBLE);
        findViewById(R.id.Continuous).setVisibility(View.INVISIBLE);

        handler.post(() -> players[turn].play());
    }

    /*void update(int position, int player) {

        String message = "";
        int distance = abs(position - gopher);

        if (position == gopher) message = "Success, player " + player + " wins!";

        else {

            if (distance == 1 || (distance >= 9 && distance <= 11)) message = "Player " + player + " - Near miss";
            else if (distance == 2 || (distance >= 8 && distance <= 12) || (distance >= 18 && distance <= 22)) message = "Player " + player + " - Close guess";
            else message = "Complete miss";

            if (board[position] != 0) message += " - Disaster";
        }

        status.setText(message);
    }*/

    void proximity() {

        // Near miss
        if (gopher % 10 != 0) near.add(gopher - 1);
        if (gopher % 10 != 9) near.add(gopher + 1);

        if (gopher > 9) near.add(gopher - 10);
        if (gopher < 90) near.add(gopher + 10);

        // Near miss diagonals
        if (gopher % 10 != 0 && gopher > 9) near.add(gopher - 11);
        if (gopher % 10 != 0 && gopher < 90) near.add(gopher + 9);

        if (gopher % 10 != 9 && gopher > 9) near.add(gopher - 9);
        if (gopher % 10 != 9 && gopher < 90) near.add(gopher + 11);

        // Close guess
        if (gopher % 10 > 1) far.add(gopher - 2);
        if (gopher % 10 < 8) far.add(gopher + 2);

        if (gopher > 19) far.add(gopher - 20);
        if (gopher < 80) far.add(gopher + 20);

        // Close guess diagonals
        if (gopher % 10 > 1 && gopher > 19) far.add(gopher - 22);
        if (gopher % 10 > 1 && gopher < 80) far.add(gopher + 18);

        if (gopher % 10 < 8 && gopher > 19) far.add(gopher - 18);
        if (gopher % 10 < 8 && gopher < 80) far.add(gopher + 22);
    }
}
