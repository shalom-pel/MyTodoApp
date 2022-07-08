package com.cognizant.android.mytodoapp

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cognizant.android.mytodoapp.Model.TodoModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.*

abstract class MainActivity : AppCompatActivity(),UpdateAndDelete {
    lateinit var database: DatabaseReference
    var toDoList:MutableList<TodoModel>? = null
    lateinit var adapter: ToDoAdapter
    private var recyclerViewItem : RecyclerView?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val fab = findViewById<View>(R.id.fab) as FloatingActionButton
        recyclerViewItem = findViewById<RecyclerView>(R.id.todoRecyclerView)

        database = FirebaseDatabase.getInstance().reference

        fab.setOnClickListener { view ->
            val alertDialog = AlertDialog.Builder(this)
            val textEditText = EditText(this)
            alertDialog.setMessage("Add TODO Item")
            alertDialog.setTitle("Enter To Do Item")
            alertDialog.setView(textEditText)
            alertDialog.setPositiveButton("Add") { dialog, i ->
                val todoItemData = TodoModel.createList()
                todoItemData.itemDataText = textEditText.text.toString()
                todoItemData.done = false

                val newItemData = database.child("todo").push()
                todoItemData.UID = newItemData.key

                newItemData.setValue(todoItemData)

                dialog.dismiss()
                Toast.makeText(this, "item saved", Toast.LENGTH_LONG).show()
            }
            alertDialog.show()
        }

        toDoList = mutableListOf<TodoModel>()
        adapter = ToDoAdapter(this, toDoList!!)
        recyclerViewItem!!.adapter=adapter
        database.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(applicationContext, "No item Added", Toast.LENGTH_LONG).show()

            }

            override fun onDataChange(snapshot: DataSnapshot) {
                toDoList!!.clear()
                addItemToList(snapshot)
            }
        })
    }

    private fun addItemToList(snapshot: DataSnapshot) {

        val item=snapshot.children.iterator()

        if (item.hasNext()){

            val toDoIndexedValue = item.next()
            val itemsIterator = toDoIndexedValue.children.iterator()

            while (itemsIterator.hasNext()){
                val currentItem = itemsIterator.next()
                val toDoItemData = TodoModel.createList()
                val map = currentItem.getValue() as HashMap<String,Any>

                toDoItemData.UID = currentItem.key
                toDoItemData.done = map.get("done") as Boolean?
                toDoItemData.itemDataText=map.get("itemDataText") as String?
                toDoList!!.add(toDoItemData)

            }

        }

        adapter.notifyDataSetChanged()
    }

    override fun modify(itemUID: String, isDone: Boolean) {
        val itemReference = database.child("todo").child(itemUID)
        itemReference.child("done").setValue(isDone)
    }

    override fun onItemDelete(itemUID: String) {
        val itemReference = database.child("todo").child(itemUID)
        itemReference.removeValue()
        adapter.notifyDataSetChanged()

    }

}








