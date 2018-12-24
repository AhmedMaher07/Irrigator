package com.maher.irrigator.dependency.module

import android.content.Context
import dagger.Module
import dagger.Provides

@Module
class ContextModule(context: Context) {

    var appContext = context

    @Provides
    fun context() : Context {
        return appContext
    }

}