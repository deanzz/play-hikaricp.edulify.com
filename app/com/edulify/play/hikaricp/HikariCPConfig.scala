/*
 * Copyright 2014 Edulify.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.edulify.play.hikaricp

import com.zaxxer.hikari.HikariConfig
import play.api.{Configuration, Logger}

import scala.concurrent.duration._

object HikariCPConfig {

  private def checkHikariConfig(config: Configuration) = {
    if ((config.getString("dataSourceClassName") ++ config.getString("jdbcUrl")).isEmpty) {
      throw new IllegalArgumentException(
        """HikariCP missing configuration. `dataSourceClassName` OR `jdbcUrl`
        must be present in your database configuration"""
      )
    }

    if (config.getString("username").isEmpty) {
      throw new IllegalArgumentException("You have to configure the `username` property.")
    }
  }

  def toHikariConfig(config: Configuration): HikariConfig = {
    checkHikariConfig(config)

    val hikariConfig = new HikariConfig()

    // Essentials configurations
    config.getString("dataSourceClassName") match {
      case Some(className) => hikariConfig.setDataSourceClassName(className)
      case _ => Logger.info("`dataSourceClassName` not present. Will use `jdbcUrl` instead.")
    }

    config.getString("jdbcUrl") match {
      case Some(jdbcUrl) => hikariConfig.setJdbcUrl(jdbcUrl)
      case _ => Logger.info("`jdbcUrl` not present. Pool configured from `dataSourceClassName`.")
    }

    hikariConfig.setUsername(config.getString("username").get)
    hikariConfig.setPassword(config.getString("password").getOrElse(""))

    // Frequently used
    hikariConfig.setAutoCommit(config.getBoolean("autoCommit").getOrElse(true))
    hikariConfig.setConnectionTimeout(config.getMilliseconds("connectionTimeout").getOrElse(30.seconds.toMillis))
    hikariConfig.setIdleTimeout(config.getMilliseconds("idleTimeout").getOrElse(10.minutes.toMillis))
    hikariConfig.setMaxLifetime(config.getMilliseconds("maxLifetime").getOrElse(30.minutes.toMillis))
    hikariConfig.setConnectionTestQuery(config.getString("connectionTestQuery").orNull)
    hikariConfig.setMinimumIdle(config.getInt("minimumIdle").getOrElse(10))
    hikariConfig.setMaximumPoolSize(config.getInt("maximumPoolSize").getOrElse(10))
    hikariConfig.setPoolName(config.getString("poolName").orNull)

    // Infrequently used
    hikariConfig.setInitializationFailFast(config.getBoolean("initializationFailFast").getOrElse(true))
    hikariConfig.setIsolateInternalQueries(config.getBoolean("isolateInternalQueries").getOrElse(false))
    hikariConfig.setAllowPoolSuspension(config.getBoolean("allowPoolSuspension").getOrElse(false))
    hikariConfig.setReadOnly(config.getBoolean("readOnly").getOrElse(false))
    hikariConfig.setRegisterMbeans(config.getBoolean("registerMbeans").getOrElse(false))
    hikariConfig.setCatalog(config.getString("catalog").orNull)
    hikariConfig.setConnectionInitSql(config.getString("connectionInitSql").orNull)
    hikariConfig.setTransactionIsolation(config.getString("transactionIsolation").orNull)
    hikariConfig.setValidationTimeout(config.getMilliseconds("validationTimeout").getOrElse(5.seconds.toMillis))
    hikariConfig.setLeakDetectionThreshold(config.getMilliseconds("leakDetectionThreshold").getOrElse(0))

    config.getString("driverClassName") match {
      case Some(driverClassName) => hikariConfig.setDriverClassName(driverClassName)
      case _ => // do nothing
    }

    hikariConfig
  }
}