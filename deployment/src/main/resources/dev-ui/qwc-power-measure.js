import {html, QwcHotReloadElement} from 'qwc-hot-reload-element';
import {JsonRpc} from 'jsonrpc';
import {notifier} from 'notifier';
import {display} from './qwc-power-display.js';
import '@vaadin/details';
import '@vaadin/vertical-layout';
import '@vaadin/icon';
import '@vaadin/vaadin-lumo-styles/vaadin-iconset.js'

export class QwcPowerMeasure extends QwcHotReloadElement {

    jsonRpc = new JsonRpc(this);

    static properties = {
        _metadata: {state: true},
        _status: {state: true},
        _running: {state: true},
        _measure: {state: true},
    };

    constructor() {
        super();
    }

    connectedCallback() {
        super.connectedCallback();
        this.hotReload();
    }

    hotReload() {
        this.jsonRpc.metadata().then(jsonRpcResponse => this._metadata = jsonRpcResponse.result);
        this.jsonRpc.status().then(jsonRpcResponse => this._status = jsonRpcResponse.result);
        this.jsonRpc.isRunning().then(response => this._running = response.result);
    }

    render() {
        if (this._status) {
            return html`
                <vaadin-details theme="filled" opened>
                    <vaadin-details-summary slot="summary">
                        Power metadata: ${display.hilite(this._status)}
                    </vaadin-details-summary>
                    <vaadin-vertical-layout style="align-items: stretch;" theme="spacing-s padding-s">
                        ${this.renderStartOrStop()}
                        ${display.metadata(this._metadata, "System power metadata", "Couldn't retrieve metadata")}
                        ${display.displayMeasure(this._measure)}
                    </vaadin-vertical-layout>
                </vaadin-details>`;
        } else {
            return html`Info unavailable`;
        }
    }

    renderStartOrStop() {
        let iconType = this._running ? "stop" : "play";
        let label = this._running ? "Stop" : "Start";
        return html`
            <vaadin-button style="width: 10%;" theme="secondary" @click="${this._startOrStop}">
                <vaadin-icon icon="font-awesome-solid:${iconType}" slot="prefix"></vaadin-icon>
                ${label}
            </vaadin-button>`
    }

    _startOrStop() {
        let stop = this._running;
        this.jsonRpc.startOrStop({start: !stop}).then(jsonRpcResponse => {
            let msg = "Started";
            if (stop) {
                this._measure = jsonRpcResponse.result;
                msg = "Stopped (" + this._measure.samplesCount + " samples taken)";
            }

            this.hotReload();
            notifier.showInfoMessage(msg);
        });
    }
}

customElements.define('qwc-power-measure', QwcPowerMeasure);
