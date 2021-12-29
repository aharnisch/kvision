@file:JsModule("leaflet")
@file:JsNonModule

package externals.leaflet.layer.tile

import externals.leaflet.DoneCallback
import externals.leaflet.geo.Coords
import org.w3c.dom.HTMLElement

open external class TileLayer<OptionsType : TileLayerOptions>(
    urlTemplate: String,
    options: TileLayerOptions = definedExternally
) : GridLayer {
    open fun setUrl(
        url: String,
        noRedraw: Boolean = definedExternally
    ): TileLayer<OptionsType> /* this */

    open fun getTileUrl(coords: Coords): String
    open fun _tileOnLoad(done: DoneCallback, tile: HTMLElement)
    open fun _tileOnError(done: DoneCallback, tile: HTMLElement, e: Error)
    open fun _abortLoading()
    open fun _getZoomForUrl(): Number
    open var options: OptionsType
}
