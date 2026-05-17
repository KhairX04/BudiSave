package com.example.budisavev2;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class AboutFragment extends Fragment {

    public AboutFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // 1. Inflate the layout for this fragment and save it to a variable named 'view'
        View view = inflater.inflate(R.layout.fragment_about, container, false);

        // 2. Link your button variable to the ID we specified in fragment_about.xml
        Button btnGithub = view.findViewById(R.id.btnGithub);

        // 3. Set a Click Listener to wait for the user to tap the button
        btnGithub.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // ⚠️ REPLACE THIS LINK WITH YOUR ACTUAL LIVE REPOSITORY URL!
                String githubUrl = "https://github.com/KhairX04/BudiSave";

                // 4. Create an implicit intent to push the system to open a web browser
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(githubUrl));
                startActivity(intent);
            }
        });

        // 5. Finally, return the modified view containing our working click logic
        return view;
    }
}