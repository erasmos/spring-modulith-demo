@ApplicationModule(allowedDependencies = {"catalog :: ids", "loans", "loans :: ids", "members :: ids"})
package com.erasmos.springmodulithdemo.notifications;

import org.springframework.modulith.ApplicationModule;