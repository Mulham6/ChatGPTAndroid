package com.nohjunh.test.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.nohjunh.test.database.entity.ContentEntity
import com.nohjunh.test.model.GptText
import com.nohjunh.test.repository.DatabaseRepository
import com.nohjunh.test.repository.NetWorkRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

class MainViewModel : ViewModel() {

    private val databaseRepository = DatabaseRepository()
    private val netWorkRepository = NetWorkRepository()

    private var _contentList = MutableLiveData<List<ContentEntity>>()
    val contentList : LiveData<List<ContentEntity>>
        get() = _contentList

    private var _deleteCheck = MutableLiveData<Boolean>(false)
    val deleteCheck : LiveData<Boolean>
        get() = _deleteCheck

    private var _gptInsertCheck = MutableLiveData<Boolean>(false)
    val gptInsertCheck : LiveData<Boolean>
        get() = _gptInsertCheck

    fun postResponse(query : String) = viewModelScope.launch {
        val jsonObject: JsonObject = JsonObject().apply{
            // params
            addProperty("model", "gpt-3.5-turbo")
            add("messages", JsonArray().apply {
                add(JsonObject().apply {
                    addProperty("role", "user")
                    addProperty("content", query)
                })
            })
            addProperty("temperature", 0)
//            addProperty("max_tokens", 500)
//            addProperty("top_p", 1)
//            addProperty("frequency_penalty", 0.0)
//            addProperty("presence_penalty", 0.0)
        }
        Timber.tag("Request json").e("${jsonObject}")
        val response = netWorkRepository.postResponse(jsonObject!!)
        Timber.tag("Response result").e("${response.choices.get(0)}")
        // json -> object 는 fromJson
        // object -> json 은 toJson
        val gson = Gson()
        val tempjson = gson.toJson(response.choices.get(0))
        val tempgson = gson.fromJson(tempjson, GptText::class.java)
        Timber.tag("Processing results").e("${tempgson.message.content}")
        insertContent(tempgson.message.content, 1)
    }

    fun getContentData() = viewModelScope.launch(Dispatchers.IO) {
        _contentList.postValue(databaseRepository.getContentData())
        _deleteCheck.postValue(false)
        _gptInsertCheck.postValue(false)
    }

    fun insertContent(content : String, gptOrUser : Int) = viewModelScope.launch(Dispatchers.IO) {
        databaseRepository.insertContent(content, gptOrUser)
        if (gptOrUser == 1) {
            _gptInsertCheck.postValue(true)
        }
    }

    fun deleteSelectedContent(id : Int) = viewModelScope.launch(Dispatchers.IO) {
        databaseRepository.deleteSelectedContent(id)
        _deleteCheck.postValue(true)
    }

}