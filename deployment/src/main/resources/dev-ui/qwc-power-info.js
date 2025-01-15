import {QwcHotReloadElement, css, html} from 'qwc-hot-reload-element';
import {JsonRpc} from 'jsonrpc';
import '@vaadin/details';
import '@vaadin/list-box';
import '@vaadin/item';
import '@vaadin/horizontal-layout';
import '@vaadin/vertical-layout';
import '@vaadin/icon';
import '@vaadin/vaadin-lumo-styles/vaadin-iconset.js'
import '@vaadin/form-layout';
import '@vaadin/text-field';
import 'qui-badge';

export class QwcPowerInfo extends QwcHotReloadElement {

    jsonRpc = new JsonRpc(this);

    static properties = {
        _remoteMetadata: {state: true},
        _localMetadata: {state: true},
        _status: {state: true}
    };

    constructor() {
        super();
    }

    connectedCallback() {
        super.connectedCallback();
        this.hotReload();
    }

    hotReload() {
        this.jsonRpc.remoteMetadata().then(jsonRpcResponse => this._remoteMetadata = jsonRpcResponse.result);
        this.jsonRpc.localMetadata().then(jsonRpcResponse => this._localMetadata = jsonRpcResponse.result);
        this.jsonRpc.status().then(jsonRpcResponse => this._status = jsonRpcResponse.result);
    }

    render() {
        if (this._status) {
            return html`
                <vaadin-details opened>
                    <vaadin-details-summary slot="summary">
                        <qui-badge level="contrast">Power metadata: ${clazz(this._status)}</qui-badge>
                    </vaadin-details-summary>
                    <vaadin-vertical-layout style="align-items: stretch;" theme="spacing-s padding-s">
                        ${this.metadata(this._localMetadata, "Local metadata, including synthetic components")}
                        ${this.metadata(this._remoteMetadata, "System power metadata")}
                    </vaadin-vertical-layout>
                </vaadin-details>
            `;
        } else {
            return html`Info unavailable`;
        }
    }

    metadata(metadata, title) {
        return html`
            <vaadin-details theme="filled">
                <vaadin-details-summary slot="summary">
                    ${title}
                </vaadin-details-summary>
                <vaadin-vertical-layout theme="spacing-s">
                   <pre>
                       ${metadata}
                   </pre>
                </vaadin-vertical-layout>
            </vaadin-details>`
    }

}

function clazz(msg, isPill) {
    if (msg) {
        let level = 'contrast';
        if (msg.startsWith('error')) {
            level = 'error';
        } else if (msg.startsWith('started')) {
            level = 'success';
        } else if (msg.startsWith('stopped')) {
            level = 'warning';
        }
        return isPill ?
            html`<qui-badge level="${level}" small pill>${msg}</qui-badge>` :
            html`<qui-badge level="${level}" small>${msg}</qui-badge>`
    }
}

customElements.define('qwc-power-info', QwcPowerInfo);
