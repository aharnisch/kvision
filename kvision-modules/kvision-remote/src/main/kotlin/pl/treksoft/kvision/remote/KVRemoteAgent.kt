/*
 * Copyright (c) 2017-present Robert Jaros
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package pl.treksoft.kvision.remote

import kotlinx.coroutines.Job
import kotlinx.coroutines.asDeferred
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.list
import kotlinx.serialization.serializer
import kotlinx.serialization.stringify
import kotlin.js.console
import kotlin.js.js
import kotlin.reflect.KClass
import kotlin.js.JSON as NativeJSON

/**
 * Client side agent for JSON-RPC remote calls.
 */
@Suppress("LargeClass", "TooManyFunctions")
@UseExperimental(ImplicitReflectionSerializer::class)
open class KVRemoteAgent<T : Any>(val serviceManager: KVServiceManager<T>) : RemoteAgent {

    val callAgent = CallAgent()

    /**
     * Executes defined call to a remote web service.
     */
    suspend inline fun <reified RET : Any, T> call(noinline function: suspend T.() -> RET): RET {
        val (url, method) =
            serviceManager.getCalls()[function.toString().replace("\\s".toRegex(), "")]
                ?: throw IllegalStateException("Function not specified!")
        return callAgent.jsonRpcCall(url, method = method).then {
            try {
                @Suppress("UNCHECKED_CAST")
                deserialize<RET>(it, RET::class.js.name)
            } catch (t: NotStandardTypeException) {
                try {
                    @Suppress("UNCHECKED_CAST")
                    tryDeserializeEnum(RET::class as KClass<Any>, it) as RET
                } catch (t: NotEnumTypeException) {
                    JSON.nonstrict.parse(RET::class.serializer(), it)
                }
            }
        }.asDeferred().await()
    }

    /**
     * Executes defined call to a remote web service.
     */
    suspend inline fun <reified RET : Any, T> call(
        noinline function: suspend T.() -> List<RET>
    ): List<RET> {
        val (url, method) =
            serviceManager.getCalls()[function.toString().replace("\\s".toRegex(), "")]
                ?: throw IllegalStateException("Function not specified!")
        return callAgent.jsonRpcCall(url, method = method).then {
            try {
                deserializeList<RET>(it, RET::class.js.name)
            } catch (t: NotStandardTypeException) {
                try {
                    @Suppress("UNCHECKED_CAST")
                    tryDeserializeEnumList(RET::class as KClass<Any>, it) as List<RET>
                } catch (t: NotEnumTypeException) {
                    JSON.nonstrict.parse(RET::class.serializer().list, it)
                }
            }
        }.asDeferred().await()
    }

    /**
     * Executes defined call to a remote web service.
     */
    suspend inline fun <reified PAR, reified RET : Any, T> call(
        noinline function: suspend T.(PAR) -> RET, p: PAR
    ): RET {
        val data = serialize(p)
        val (url, method) =
            serviceManager.getCalls()[function.toString().replace("\\s".toRegex(), "")]
                ?: throw IllegalStateException("Function not specified!")
        return callAgent.jsonRpcCall(url, listOf(data), method).then {
            try {
                @Suppress("UNCHECKED_CAST")
                deserialize<RET>(it, RET::class.js.name)
            } catch (t: NotStandardTypeException) {
                try {
                    @Suppress("UNCHECKED_CAST")
                    tryDeserializeEnum(RET::class as KClass<Any>, it) as RET
                } catch (t: NotEnumTypeException) {
                    JSON.nonstrict.parse(RET::class.serializer(), it)
                }
            }
        }.asDeferred().await()
    }

    /**
     * Executes defined call to a remote web service.
     */
    suspend inline fun <reified PAR, reified RET : Any, T> call(
        noinline function: suspend T.(PAR) -> List<RET>, p: PAR
    ): List<RET> {
        val data = serialize(p)
        val (url, method) =
            serviceManager.getCalls()[function.toString().replace("\\s".toRegex(), "")]
                ?: throw IllegalStateException("Function not specified!")
        return callAgent.jsonRpcCall(url, listOf(data), method).then {
            try {
                deserializeList<RET>(it, RET::class.js.name)
            } catch (t: NotStandardTypeException) {
                try {
                    @Suppress("UNCHECKED_CAST")
                    tryDeserializeEnumList(RET::class as KClass<Any>, it) as List<RET>
                } catch (t: NotEnumTypeException) {
                    JSON.nonstrict.parse(RET::class.serializer().list, it)
                }
            }
        }.asDeferred().await()
    }

    /**
     * Executes defined call to a remote web service.
     */
    suspend inline fun <reified PAR1, reified PAR2, reified RET : Any, T> call(
        noinline function: suspend T.(PAR1, PAR2) -> RET, p1: PAR1, p2: PAR2
    ): RET {
        val data1 = serialize(p1)
        val data2 = serialize(p2)
        val (url, method) =
            serviceManager.getCalls()[function.toString().replace("\\s".toRegex(), "")]
                ?: throw IllegalStateException("Function not specified!")
        return callAgent.jsonRpcCall(url, listOf(data1, data2), method).then {
            try {
                @Suppress("UNCHECKED_CAST")
                deserialize<RET>(it, RET::class.js.name)
            } catch (t: NotStandardTypeException) {
                try {
                    @Suppress("UNCHECKED_CAST")
                    tryDeserializeEnum(RET::class as KClass<Any>, it) as RET
                } catch (t: NotEnumTypeException) {
                    JSON.nonstrict.parse(RET::class.serializer(), it)
                }
            }
        }.asDeferred().await()
    }

    /**
     * Executes defined call to a remote web service.
     */
    suspend inline fun <reified PAR1, reified PAR2, reified RET : Any, T> call(
        noinline function: suspend T.(PAR1, PAR2) -> List<RET>, p1: PAR1, p2: PAR2
    ): List<RET> {
        val data1 = serialize(p1)
        val data2 = serialize(p2)
        val (url, method) =
            serviceManager.getCalls()[function.toString().replace("\\s".toRegex(), "")]
                ?: throw IllegalStateException("Function not specified!")
        return callAgent.jsonRpcCall(url, listOf(data1, data2), method).then {
            try {
                deserializeList<RET>(it, RET::class.js.name)
            } catch (t: NotStandardTypeException) {
                try {
                    @Suppress("UNCHECKED_CAST")
                    tryDeserializeEnumList(RET::class as KClass<Any>, it) as List<RET>
                } catch (t: NotEnumTypeException) {
                    JSON.nonstrict.parse(RET::class.serializer().list, it)
                }
            }
        }.asDeferred().await()
    }

    /**
     * Executes defined call to a remote web service.
     */
    suspend inline fun <reified PAR1, reified PAR2, reified PAR3, reified RET : Any, T> call(
        noinline function: suspend T.(PAR1, PAR2, PAR3) -> RET, p1: PAR1, p2: PAR2, p3: PAR3
    ): RET {
        val data1 = serialize(p1)
        val data2 = serialize(p2)
        val data3 = serialize(p3)
        val (url, method) =
            serviceManager.getCalls()[function.toString().replace("\\s".toRegex(), "")]
                ?: throw IllegalStateException("Function not specified!")
        return callAgent.jsonRpcCall(url, listOf(data1, data2, data3), method).then {
            try {
                @Suppress("UNCHECKED_CAST")
                deserialize<RET>(it, RET::class.js.name)
            } catch (t: NotStandardTypeException) {
                try {
                    @Suppress("UNCHECKED_CAST")
                    tryDeserializeEnum(RET::class as KClass<Any>, it) as RET
                } catch (t: NotEnumTypeException) {
                    JSON.nonstrict.parse(RET::class.serializer(), it)
                }
            }
        }.asDeferred().await()
    }

    /**
     * Executes defined call to a remote web service.
     */
    suspend inline fun <reified PAR1, reified PAR2, reified PAR3, reified RET : Any, T> call(
        noinline function: suspend T.(PAR1, PAR2, PAR3) -> List<RET>, p1: PAR1, p2: PAR2, p3: PAR3
    ): List<RET> {
        val data1 = serialize(p1)
        val data2 = serialize(p2)
        val data3 = serialize(p3)
        val (url, method) =
            serviceManager.getCalls()[function.toString().replace("\\s".toRegex(), "")]
                ?: throw IllegalStateException("Function not specified!")
        return callAgent.jsonRpcCall(url, listOf(data1, data2, data3), method).then {
            try {
                deserializeList<RET>(it, RET::class.js.name)
            } catch (t: NotStandardTypeException) {
                try {
                    @Suppress("UNCHECKED_CAST")
                    tryDeserializeEnumList(RET::class as KClass<Any>, it) as List<RET>
                } catch (t: NotEnumTypeException) {
                    JSON.nonstrict.parse(RET::class.serializer().list, it)
                }
            }
        }.asDeferred().await()
    }

    /**
     * Executes defined call to a remote web service.
     */
    suspend inline fun <reified PAR1, reified PAR2, reified PAR3, reified PAR4, reified RET : Any, T> call(
        noinline function: suspend T.(PAR1, PAR2, PAR3, PAR4) -> RET, p1: PAR1, p2: PAR2, p3: PAR3, p4: PAR4
    ): RET {
        val data1 = serialize(p1)
        val data2 = serialize(p2)
        val data3 = serialize(p3)
        val data4 = serialize(p4)
        val (url, method) =
            serviceManager.getCalls()[function.toString().replace("\\s".toRegex(), "")]
                ?: throw IllegalStateException("Function not specified!")
        return callAgent.jsonRpcCall(url, listOf(data1, data2, data3, data4), method).then {
            try {
                @Suppress("UNCHECKED_CAST")
                deserialize<RET>(it, RET::class.js.name)
            } catch (t: NotStandardTypeException) {
                try {
                    @Suppress("UNCHECKED_CAST")
                    tryDeserializeEnum(RET::class as KClass<Any>, it) as RET
                } catch (t: NotEnumTypeException) {
                    JSON.nonstrict.parse(RET::class.serializer(), it)
                }
            }
        }.asDeferred().await()
    }

    /**
     * Executes defined call to a remote web service.
     */
    suspend inline fun <reified PAR1, reified PAR2, reified PAR3, reified PAR4, reified RET : Any, T> call(
        noinline function: suspend T.(PAR1, PAR2, PAR3, PAR4) -> List<RET>,
        p1: PAR1,
        p2: PAR2,
        p3: PAR3,
        p4: PAR4
    ): List<RET> {
        val data1 = serialize(p1)
        val data2 = serialize(p2)
        val data3 = serialize(p3)
        val data4 = serialize(p4)
        val (url, method) =
            serviceManager.getCalls()[function.toString().replace("\\s".toRegex(), "")]
                ?: throw IllegalStateException("Function not specified!")
        return callAgent.jsonRpcCall(url, listOf(data1, data2, data3, data4), method).then {
            try {
                deserializeList<RET>(it, RET::class.js.name)
            } catch (t: NotStandardTypeException) {
                try {
                    @Suppress("UNCHECKED_CAST")
                    tryDeserializeEnumList(RET::class as KClass<Any>, it) as List<RET>
                } catch (t: NotEnumTypeException) {
                    JSON.nonstrict.parse(RET::class.serializer().list, it)
                }
            }
        }.asDeferred().await()
    }

    /**
     * Executes defined call to a remote web service.
     */
    @Suppress("LongParameterList")
    suspend inline fun <reified PAR1, reified PAR2, reified PAR3, reified PAR4, reified PAR5,
            reified RET : Any, T> call(
        noinline function: suspend T.(PAR1, PAR2, PAR3, PAR4, PAR5) -> RET,
        p1: PAR1,
        p2: PAR2,
        p3: PAR3,
        p4: PAR4,
        p5: PAR5
    ): RET {
        val data1 = serialize(p1)
        val data2 = serialize(p2)
        val data3 = serialize(p3)
        val data4 = serialize(p4)
        val data5 = serialize(p5)
        val (url, method) =
            serviceManager.getCalls()[function.toString().replace("\\s".toRegex(), "")]
                ?: throw IllegalStateException("Function not specified!")
        return callAgent.jsonRpcCall(url, listOf(data1, data2, data3, data4, data5), method).then {
            try {
                @Suppress("UNCHECKED_CAST")
                deserialize<RET>(it, RET::class.js.name)
            } catch (t: NotStandardTypeException) {
                try {
                    @Suppress("UNCHECKED_CAST")
                    tryDeserializeEnum(RET::class as KClass<Any>, it) as RET
                } catch (t: NotEnumTypeException) {
                    JSON.nonstrict.parse(RET::class.serializer(), it)
                }
            }
        }.asDeferred().await()
    }

    /**
     * Executes defined call to a remote web service.
     */
    @Suppress("LongParameterList")
    suspend inline fun <reified PAR1, reified PAR2, reified PAR3, reified PAR4, reified PAR5,
            reified RET : Any, T> call(
        noinline function: suspend T.(PAR1, PAR2, PAR3, PAR4, PAR5) -> List<RET>,
        p1: PAR1,
        p2: PAR2,
        p3: PAR3,
        p4: PAR4,
        p5: PAR5
    ): List<RET> {
        val data1 = serialize(p1)
        val data2 = serialize(p2)
        val data3 = serialize(p3)
        val data4 = serialize(p4)
        val data5 = serialize(p5)
        val (url, method) =
            serviceManager.getCalls()[function.toString().replace("\\s".toRegex(), "")]
                ?: throw IllegalStateException("Function not specified!")
        return callAgent.jsonRpcCall(url, listOf(data1, data2, data3, data4, data5), method).then {
            try {
                deserializeList<RET>(it, RET::class.js.name)
            } catch (t: NotStandardTypeException) {
                try {
                    @Suppress("UNCHECKED_CAST")
                    tryDeserializeEnumList(RET::class as KClass<Any>, it) as List<RET>
                } catch (t: NotEnumTypeException) {
                    JSON.nonstrict.parse(RET::class.serializer().list, it)
                }
            }
        }.asDeferred().await()
    }

    /**
     * Executes defined web socket connection
     */
    suspend inline fun <reified PAR1 : Any, reified PAR2 : Any> webSocket(
        noinline function: suspend T.(ReceiveChannel<PAR1>, SendChannel<PAR2>) -> Unit,
        noinline handler: suspend (SendChannel<PAR1>, ReceiveChannel<PAR2>) -> Unit
    ) {
        val (url, _) =
            serviceManager.getCalls()[function.toString().replace("\\s".toRegex(), "")]
                ?: throw IllegalStateException("Function not specified!")
        val socket = Socket()
        val requestChannel = Channel<PAR1>()
        val responseChannel = Channel<PAR2>()
        try {
            coroutineScope {
                socket.connect(getWebSocketUrl(url))
                lateinit var responseJob: Job
                lateinit var handlerJob: Job
                val requestJob = launch {
                    for (par1 in requestChannel) {
                        val param = serialize(par1)
                        val str = JSON.plain.stringify(JsonRpcRequest(0, url, listOf(param)))
                        if (!socket.sendOrFalse(str)) break
                    }
                    responseJob.cancel()
                    handlerJob.cancel()
                    if (!requestChannel.isClosedForReceive) requestChannel.close()
                    if (!responseChannel.isClosedForSend) responseChannel.close()
                }
                responseJob = launch {
                    while (true) {
                        val str = socket.receiveOrNull() ?: break
                        val data = kotlin.js.JSON.parse<dynamic>(str).result
                        val par2 = try {
                            @Suppress("UNCHECKED_CAST")
                            deserialize<PAR2>(data, PAR2::class.js.name)
                        } catch (t: NotStandardTypeException) {
                            try {
                                @Suppress("UNCHECKED_CAST")
                                tryDeserializeEnum(PAR2::class as KClass<Any>, data) as PAR2
                            } catch (t: NotEnumTypeException) {
                                JSON.nonstrict.parse(PAR2::class.serializer(), data)
                            }
                        }
                        responseChannel.send(par2)
                    }
                    requestJob.cancel()
                    handlerJob.cancel()
                    if (!requestChannel.isClosedForReceive) requestChannel.close()
                    if (!responseChannel.isClosedForSend) responseChannel.close()
                }
                handlerJob = launch {
                    exceptionHelper {
                        handler(requestChannel, responseChannel)
                    }
                    requestJob.cancel()
                    responseJob.cancel()
                    if (!requestChannel.isClosedForReceive) requestChannel.close()
                    if (!responseChannel.isClosedForSend) responseChannel.close()
                }
            }
        } catch (e: Exception) {
            console.log(e)
        }
        if (!requestChannel.isClosedForReceive) requestChannel.close()
        if (!responseChannel.isClosedForSend) responseChannel.close()
        socket.close()
    }

    /**
     * Executes defined web socket connection returning list objects
     */
    suspend inline fun <reified PAR1 : Any, reified PAR2 : Any> webSocket(
        noinline function: suspend T.(ReceiveChannel<PAR1>, SendChannel<List<PAR2>>) -> Unit,
        noinline handler: suspend (SendChannel<PAR1>, ReceiveChannel<List<PAR2>>) -> Unit
    ) {
        val (url, _) =
            serviceManager.getCalls()[function.toString().replace("\\s".toRegex(), "")]
                ?: throw IllegalStateException("Function not specified!")
        val socket = Socket()
        val requestChannel = Channel<PAR1>()
        val responseChannel = Channel<List<PAR2>>()
        try {
            coroutineScope {
                socket.connect(getWebSocketUrl(url))
                lateinit var responseJob: Job
                lateinit var handlerJob: Job
                val requestJob = launch {
                    for (par1 in requestChannel) {
                        val param = serialize(par1)
                        val str = JSON.plain.stringify(JsonRpcRequest(0, url, listOf(param)))
                        if (!socket.sendOrFalse(str)) break
                    }
                    responseJob.cancel()
                    handlerJob.cancel()
                    if (!requestChannel.isClosedForReceive) requestChannel.close()
                    if (!responseChannel.isClosedForSend) responseChannel.close()
                }
                responseJob = launch {
                    while (true) {
                        val str = socket.receiveOrNull() ?: break
                        val data = kotlin.js.JSON.parse<dynamic>(str).result
                        val par2 = try {
                            deserializeList<PAR2>(data, PAR2::class.js.name)
                        } catch (t: NotStandardTypeException) {
                            try {
                                @Suppress("UNCHECKED_CAST")
                                tryDeserializeEnumList(PAR2::class as KClass<Any>, data) as List<PAR2>
                            } catch (t: NotEnumTypeException) {
                                JSON.nonstrict.parse(PAR2::class.serializer().list, data)
                            }
                        }
                        responseChannel.send(par2)
                    }
                    requestJob.cancel()
                    handlerJob.cancel()
                    if (!requestChannel.isClosedForReceive) requestChannel.close()
                    if (!responseChannel.isClosedForSend) responseChannel.close()
                }
                handlerJob = launch {
                    exceptionHelper {
                        handler(requestChannel, responseChannel)
                    }
                    requestJob.cancel()
                    responseJob.cancel()
                    if (!requestChannel.isClosedForReceive) requestChannel.close()
                    if (!responseChannel.isClosedForSend) responseChannel.close()
                }
            }
        } catch (e: Exception) {
            console.log(e)
        }
        if (!requestChannel.isClosedForReceive) requestChannel.close()
        if (!responseChannel.isClosedForSend) responseChannel.close()
        socket.close()
    }

    /**
     * @suppress internal function
     */
    suspend fun Socket.receiveOrNull(): String? {
        return try {
            this.receive()
        } catch (e: SocketClosedException) {
            console.log("Socket was closed: ${e.reason}")
            null
        }
    }

    /**
     * @suppress internal function
     */
    fun Socket.sendOrFalse(str: String): Boolean {
        return try {
            this.send(str)
            true
        } catch (e: SocketClosedException) {
            console.log("Socket was closed: ${e.reason}")
            false
        }
    }

    /**
     * @suppress internal function
     */
    suspend fun exceptionHelper(block: suspend () -> Unit) {
        try {
            block()
        } catch (e: Exception) {
            console.log(e)
        }
    }

    /**
     * @suppress
     * Internal function
     */
    inline fun <reified PAR> serialize(value: PAR): String? {
        return value?.let {
            @Suppress("UNCHECKED_CAST")
            trySerialize((PAR::class as KClass<Any>), it as Any)
        }
    }

}
