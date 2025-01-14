import {LitElement, html, css} from 'lit';
import {JsonRpc} from 'jsonrpc';

export class QwcPowerInfo extends LitElement {

    jsonRpc = new JsonRpc(this);

    static properties = {
        _info: {state: true},
    };

    constructor() {
        super();
    }

    connectedCallback() {
        super.connectedCallback();
        this.hotReload();
    }

    hotReload() {
        this.jsonRpc.info().then(
            jsonRpcResponse => this._info = jsonRpcResponse.result);
    }

    render() {
        if (this._info) {
            return html`<pre>${this._info}</pre>`;
        } else {
            return html`Info unavailable`;
        }
    }
}

customElements.define('qwc-power-info', QwcPowerInfo);
