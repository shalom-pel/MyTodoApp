package com.cognizant.android.mytodoapp

interface UpdateAndDelete{

    fun modify(itemUID : String ,isDone : Boolean)
    fun onItemDelete(itemUID: String)

}