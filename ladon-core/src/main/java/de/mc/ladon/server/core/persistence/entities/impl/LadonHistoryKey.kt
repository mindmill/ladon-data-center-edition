/*
 * Copyright (c) 2016 Mind Consulting UG(haftungsbeschränkt)
 */

package de.mc.ladon.server.core.persistence.entities.impl

import de.mc.ladon.server.core.api.persistence.entities.HistoryKey

/**
 * @author Ralf Ulrich
 * 16.09.16
 */
data class LadonHistoryKey(override val repositoryId: String,
                           override val versionSeriesId: String ): HistoryKey