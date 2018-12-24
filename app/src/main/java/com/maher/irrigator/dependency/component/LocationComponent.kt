package com.maher.irrigator.dependency.component

import com.maher.irrigator.dependency.module.LocationModule
import com.maher.irrigator.dependency.scope.WeatherScope
import com.maher.irrigator.DetailsActivity
import dagger.Component

@WeatherScope
@Component(modules = [LocationModule::class])
interface LocationComponent {
    fun inject(detailsActivity: DetailsActivity)
}