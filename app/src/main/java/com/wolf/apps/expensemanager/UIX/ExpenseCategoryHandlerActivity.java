package com.wolf.apps.expensemanager.UIX;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.wolf.apps.expensemanager.MainActivity;
import com.wolf.apps.expensemanager.Models.ExpenseCategory;
import com.wolf.apps.expensemanager.Models.ExpenseDbManager;
import com.wolf.apps.expensemanager.Models.ExpenseSubCategory;
import com.wolf.apps.expensemanager.R;

import java.util.List;

public class ExpenseCategoryHandlerActivity extends AppCompatActivity implements View.OnClickListener{

    ExpenseDbManager db;
    static ExpenseCategory current_expense_category;
    static Boolean mode;
    TextView txt_Name;
    TextView txt_ID;
    Button btn_Save;
    Button btn_Delete;
    FloatingActionButton fab_addSubCat;
    ListView sub_category_list;
    ArrayAdapter<ExpenseSubCategory>  list_adapter;

    public ExpenseCategoryHandlerActivity() {
        super();
        if(mode == null){
            mode = false;
        }
        db = new ExpenseDbManager(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_handler);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_CategoryHandler);
        toolbar.setTitleTextColor(Color.WHITE);
        toolbar.setTitle("Expense Categories");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        txt_Name = (TextView) findViewById(R.id.txt_CategoryHandlerName);
        txt_ID = (TextView) findViewById(R.id.txt_CategoryHandlerID);
        btn_Save = (Button)findViewById(R.id.btn_CategoryHandlerSave);
        btn_Delete = (Button)findViewById(R.id.btn_CategoryHandlerDelete);
        fab_addSubCat = (FloatingActionButton)findViewById(R.id.fab_CategoryHandlerAddSubCategory);
        btn_Save.setOnClickListener(this);
        btn_Delete.setOnClickListener(this);
        fab_addSubCat.setOnClickListener(this);
        sub_category_list = (ListView)findViewById(R.id.list_CategoryHandlerSubCategories);

        if(mode == false){
            txt_ID.setText(Integer.toString(ExpenseCategory.getLastID(db.getReadableDatabase())));
        }
        else{
            txt_ID.setText(Integer.toString(current_expense_category.getId()));
            txt_Name.setText(current_expense_category.getDescription());
            list_adapter = new ExpenseSubArrayAdapter(this, ExpenseSubCategory.getByCategory(current_expense_category, db.getReadableDatabase()));
            sub_category_list.setAdapter(list_adapter);

        }

    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent upIntent = new Intent(this, MainActivity.class);
                upIntent.putExtra("OpenFragment", 2);
                NavUtils.navigateUpTo(this, upIntent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private void removeCurrentExpenseCategory(){
        if(current_expense_category != null){
            current_expense_category.remove(db.getWritableDatabase());
        }
    }

    public static void enableUpdate(ExpenseCategory expenseCategory){
        current_expense_category = expenseCategory;
        mode = true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mode = null;
        current_expense_category = null;
    }

    @Override
    public void onClick(View v) {
        final String[] temp = new String[1];
        switch (v.getId()){
            case R.id.btn_CategoryHandlerSave:
                if(!txt_Name.getText().toString().matches("")){
                    if(mode == false){
                        new ExpenseCategory(Integer.parseInt(txt_ID.getText().toString()), txt_Name.getText().toString()).add(db.getWritableDatabase());
                        finish();
                        return;
                    }
                    current_expense_category.setDescription(this.txt_Name.getText().toString());
                    current_expense_category.update(db.getWritableDatabase());
                    finish();
                }
                return;
            case R.id.btn_CategoryHandlerDelete:
                if(mode == true){
                    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which){
                                case DialogInterface.BUTTON_POSITIVE:
                                    removeCurrentExpenseCategory();
                                    finish();
                                    break;

                                case DialogInterface.BUTTON_NEGATIVE:
                                    //No button clicked
                                    break;
                            }
                        }
                    };
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setMessage("Delete this category? Are you sure?").setPositiveButton("Yes", dialogClickListener)
                            .setNegativeButton("No", dialogClickListener).show();
                    return;
                }

            case R.id.fab_CategoryHandlerAddSubCategory:
                if(!txt_Name.getText().toString().matches("")){
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Enter Sub Category Name");
                    final EditText input = new EditText(this);
                    input.setInputType(InputType.TYPE_CLASS_TEXT);
                    builder.setView(input);
                    builder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            temp[0] = input.getText().toString();
                            if(mode == false){
                                current_expense_category = new ExpenseCategory(Integer.parseInt(txt_ID.getText().toString()), txt_Name.getText().toString());
                                current_expense_category.add(db.getWritableDatabase());
                                mode = true;
                                new ExpenseSubCategory(0,temp[0],current_expense_category).add(db.getWritableDatabase());
                                list_adapter = new ExpenseSubArrayAdapter(ExpenseCategoryHandlerActivity.this, ExpenseSubCategory.getByCategory(current_expense_category, db.getReadableDatabase()));
                                sub_category_list.setAdapter(list_adapter);
                                list_adapter.notifyDataSetChanged();
                                return;
                            }

                            current_expense_category.setDescription(txt_Name.getText().toString());
                            current_expense_category.update(db.getWritableDatabase());
                            new ExpenseSubCategory(0, temp[0], current_expense_category).add(db.getWritableDatabase());
                            list_adapter = new ExpenseSubArrayAdapter(ExpenseCategoryHandlerActivity.this, ExpenseSubCategory.getByCategory(current_expense_category, db.getReadableDatabase()));
                            sub_category_list.setAdapter(list_adapter);
                            list_adapter.notifyDataSetChanged();
                        }
                    });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    builder.show();
                }
                return;
            }
    }


    class ExpenseSubArrayAdapter extends ArrayAdapter<ExpenseSubCategory>{

        public ExpenseSubArrayAdapter(@NonNull Context context, @NonNull List<ExpenseSubCategory> objects) {
            super(context, 0, objects);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            final ExpenseSubCategory expenseSubCategory = getItem(position);

            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.sub_category_holder, parent, false);
            }

            final TextView txt_name = convertView.findViewById(R.id.txt_SubCategoryHolderName);
            txt_name.setText(expenseSubCategory.getDescription());
            ImageButton btn_edit = convertView.findViewById(R.id.btn_SubCategoryHolderEdit);
            ImageButton btn_delete = convertView.findViewById(R.id.btn_SubCategoryHolderDelete);

            btn_edit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final String[] temp = new String[1];

                    AlertDialog.Builder builder = new AlertDialog.Builder(ExpenseCategoryHandlerActivity.this);
                    builder.setTitle("Enter Sub Category Name");
                    final EditText input = new EditText(ExpenseCategoryHandlerActivity.this);
                    input.setInputType(InputType.TYPE_CLASS_TEXT);
                    input.setText(expenseSubCategory.getDescription());
                    builder.setView(input);
                    builder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            temp[0] = input.getText().toString();
                            expenseSubCategory.setDescription(temp[0]);
                            expenseSubCategory.update(db.getWritableDatabase());
                            list_adapter = new ExpenseSubArrayAdapter(ExpenseCategoryHandlerActivity.this, ExpenseSubCategory.getByCategory(current_expense_category, db.getReadableDatabase()));
                            sub_category_list.setAdapter(list_adapter);
                            list_adapter.notifyDataSetChanged();
                        }
                    });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    builder.show();
                }
            });

            btn_delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which){
                                case DialogInterface.BUTTON_POSITIVE:
                                    expenseSubCategory.remove(db.getWritableDatabase());
                                    list_adapter = new ExpenseSubArrayAdapter(ExpenseCategoryHandlerActivity.this, ExpenseSubCategory.getByCategory(current_expense_category, db.getReadableDatabase()));
                                    sub_category_list.setAdapter(list_adapter);
                                    list_adapter.notifyDataSetChanged();
                                    break;

                                case DialogInterface.BUTTON_NEGATIVE:
                                    //No button clicked
                                    break;
                            }
                        }
                    };
                    AlertDialog.Builder builder = new AlertDialog.Builder(ExpenseCategoryHandlerActivity.this);
                    builder.setMessage("Delete this sub category? Are you sure?").setPositiveButton("Yes", dialogClickListener)
                            .setNegativeButton("No", dialogClickListener).show();
                }
            });

            return convertView;
        }
    }
}
