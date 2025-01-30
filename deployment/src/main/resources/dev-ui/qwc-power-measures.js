import {html, QwcHotReloadElement} from 'qwc-hot-reload-element';
import {JsonRpc} from 'jsonrpc';
import {display} from './qwc-power-display.js';
import '@vaadin/details';
import '@vaadin/vertical-layout';
import '@vaadin/icon';
import '@vaadin/vaadin-lumo-styles/vaadin-iconset.js'

export class QwcPowerMeasures extends QwcHotReloadElement {

    jsonRpc = new JsonRpc(this);

    static properties = {
        _measures: {state: true}
    };

    constructor() {
        super();
    }

    connectedCallback() {
        super.connectedCallback();
        this.hotReload();
    }

    hotReload() {
        this.jsonRpc.measures().then(jsonRpcResponse => this._measures = jsonRpcResponse.result);
    }

    render() {
        if (this._measures) {
            return html`
                <vaadin-details theme="filled" opened>
                    <vaadin-details-summary slot="summary">
                        Recorded Measures
                    </vaadin-details-summary>
                    <vaadin-vertical-layout style="align-items: stretch;" theme="spacing-s padding-s">
                       ${display.measures(this._measures)}
                    </vaadin-vertical-layout>
                </vaadin-details>`;
        } else {
            return html`No recorded measures`;
        }
    }
}

customElements.define('qwc-power-measures', QwcPowerMeasures);
