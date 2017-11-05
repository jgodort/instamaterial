package com.softonic.instamaterial.data.repository.loggedUser;

import com.softonic.instamaterial.domain.common.ObservableTask;

/**
 * Created by javie on 05/11/2017.
 */

interface AuthenticatedUserDataSource {
    ObservableTask<String> get();
}
