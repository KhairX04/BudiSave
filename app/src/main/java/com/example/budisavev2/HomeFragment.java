package com.example.budisavev2;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.fragment.app.Fragment;
import com.google.android.material.slider.Slider;
import java.util.Locale;

public class HomeFragment extends Fragment {

    Spinner spinnerType;
    EditText etAmount;
    Switch switchBudi;
    RadioGroup radioGroupMode;
    Slider sliderAmount;
    TextView tvLabelLiters, tvResultLiters, tvResultPump, tvResultSubsidy, tvFixedPrice;

    boolean isUpdating = false;
    double currentMarketPrice = 3.87; // Baseline starting market price for RON95

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Bind UI Objects to XML Layout IDs
        spinnerType = view.findViewById(R.id.spinnerType);
        etAmount = view.findViewById(R.id.etAmount);
        switchBudi = view.findViewById(R.id.switchBudi);
        radioGroupMode = view.findViewById(R.id.radioGroupMode);
        sliderAmount = view.findViewById(R.id.sliderAmount);
        tvLabelLiters = view.findViewById(R.id.tvLabelLiters);
        tvResultLiters = view.findViewById(R.id.tvResultLiters);
        tvResultPump = view.findViewById(R.id.tvResultPump);
        tvResultSubsidy = view.findViewById(R.id.tvResultSubsidy);
        tvFixedPrice = view.findViewById(R.id.tvFixedPrice);

        // Populate Dropdown Selection Array
        String[] fuels = {
                "RON95",
                "RON97",
                "Diesel (Peninsular Malaysia)",
                "Diesel (Sabah, Sarawak & Labuan)"
        };
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, fuels);
        spinnerType.setAdapter(adapter);

        spinnerType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) { // RON95
                    currentMarketPrice = 3.87;
                    tvFixedPrice.setText("Market Price: RM 3.87 / Liter");
                    switchBudi.setEnabled(true);
                } else if (position == 1) { // RON97
                    currentMarketPrice = 4.70;
                    tvFixedPrice.setText("Market Price: RM 4.70 / Liter");
                    switchBudi.setChecked(false);
                    switchBudi.setEnabled(false);
                } else if (position == 2) { // Diesel Peninsular
                    currentMarketPrice = 4.87;
                    tvFixedPrice.setText("Market Price: RM 4.87 / Liter");
                    switchBudi.setChecked(false);
                    switchBudi.setEnabled(false);
                } else { // Diesel Borneo & Labuan
                    currentMarketPrice = 2.15;
                    tvFixedPrice.setText("Market Price: RM 2.15 / Liter");
                    switchBudi.setChecked(false);
                    switchBudi.setEnabled(false);
                }
                calculate();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Handle Slider Maximum Ranges Depending on Liters vs Cash Mode
        radioGroupMode.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbLiters) {
                sliderAmount.setValueTo(200.0f); // 200 Liters Max Quota
            } else {
                sliderAmount.setValueTo(500.0f); // RM 500 Max Input
            }
            sliderAmount.setValue(0);
            etAmount.setText("");
            calculate();
        });

        // Two-Way Sync: Slider Changes Update the Text Box Value
        sliderAmount.addOnChangeListener((slider, value, fromUser) -> {
            if (fromUser) {
                isUpdating = true;
                etAmount.setText(String.format(Locale.US, "%.1f", value));
                calculate();
                isUpdating = false;
            }
        });

        // Two-Way Sync: Manual Text Typing Updates the Slider Position
        etAmount.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                if (!isUpdating && s.length() > 0) {
                    try {
                        float val = Float.parseFloat(s.toString());
                        if (val >= sliderAmount.getValueFrom() && val <= sliderAmount.getValueTo()) {
                            sliderAmount.setValue(val);
                        }
                        calculate();
                    } catch (Exception ignored) {}
                } else if (!isUpdating && s.length() == 0) {
                    sliderAmount.setValue(0);
                    calculate();
                }
            }
        });

        // Force Instant Recalculation When Eligibility Status Switches
        switchBudi.setOnCheckedChangeListener((buttonView, isChecked) -> calculate());

        return view;
    }

    // Main Engine Logic Processing the Calculations
    private void calculate() {
        // FIRST: Check if BUDI is active and update the text row label immediately!
        boolean isBudiActive = switchBudi.isChecked() && spinnerType.getSelectedItemPosition() == 0;
        double effectivePricePerLiter = isBudiActive ? 1.99 : currentMarketPrice;

        if (isBudiActive) {
            tvLabelLiters.setText("Liters @ RM1.99");
        } else {
            tvLabelLiters.setText(String.format(Locale.getDefault(), "Liters @ RM%.2f", currentMarketPrice));
        }

        // SECOND: Check if input is empty. If yes, clear metrics and exit early safely.
        if (etAmount.getText().toString().isEmpty()) {
            tvResultLiters.setText("0.000 L");
            tvResultPump.setText("RM 0.00");
            tvResultSubsidy.setText("RM 0.00");
            return;
        }

        // THIRD: Carry out math equations if amounts are present
        double inputAmount = Double.parseDouble(etAmount.getText().toString());
        boolean isLitersMode = radioGroupMode.getCheckedRadioButtonId() == R.id.rbLiters;

        double totalLiters;
        double pumpDisplayCost;

        if (isLitersMode) {
            totalLiters = inputAmount;
            pumpDisplayCost = totalLiters * currentMarketPrice;
        } else {
            double cashPaid = inputAmount;
            totalLiters = cashPaid / effectivePricePerLiter;
            pumpDisplayCost = totalLiters * currentMarketPrice;
        }

        double calculatedSubsidyValue = pumpDisplayCost - (totalLiters * effectivePricePerLiter);

        // Bind Variables to the UI Display Outputs
        tvResultLiters.setText(String.format(Locale.getDefault(), "%.3f L", totalLiters));
        tvResultPump.setText(String.format(Locale.getDefault(), "RM %.2f", pumpDisplayCost));
        tvResultSubsidy.setText(String.format(Locale.getDefault(), "RM %.2f", calculatedSubsidyValue));
    }
}