import {html, QwcHotReloadElement} from 'qwc-hot-reload-element';
import {JsonRpc} from 'jsonrpc';
import {notifier} from 'notifier';
import '@vaadin/details';
import '@vaadin/horizontal-layout';
import '@vaadin/vertical-layout';
import '@vaadin/icon';
import '@vaadin/vaadin-lumo-styles/vaadin-iconset.js'
import 'qui-badge';

export class QwcPowerInfo extends QwcHotReloadElement {

    jsonRpc = new JsonRpc(this);

    static properties = {
        _remoteMetadata: {state: true},
        _localMetadata: {state: true},
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
        this.jsonRpc.remoteMetadata().then(jsonRpcResponse => this._remoteMetadata = jsonRpcResponse.result);
        this.jsonRpc.localMetadata().then(jsonRpcResponse => this._localMetadata = jsonRpcResponse.result);
        this.jsonRpc.status().then(jsonRpcResponse => this._status = jsonRpcResponse.result);
        this.jsonRpc.isRunning().then(response => this._running = response.result);
    }

    render() {
        if (this._status) {
            return html`
                <vaadin-details opened>
                    <vaadin-details-summary slot="summary">
                        <qui-badge level="contrast">Power metadata: ${clazz(this._status)}</qui-badge>
                    </vaadin-details-summary>
                    <vaadin-vertical-layout style="align-items: stretch;" theme="spacing-s padding-s">
                        ${this.renderStartOrStop()}
                        ${this.metadata(this._localMetadata, "Local synthetic components (if any)", "No ongoing measure")}
                        ${this.metadata(this._remoteMetadata, "System power metadata", "Couldn't retrieve metadata")}
                        ${this.displayMeasures()}
                    </vaadin-vertical-layout>
                </vaadin-details>`;
        } else {
            return html`Info unavailable`;
        }
    }

    displayMeasures() {
        if (this._measure) {
            return html`
            <vaadin-details theme="filled">
                <vaadin-details-summary slot="summary">
                    Measure ${this._measure.result} (${this._measure.samplesCount} samples)
                </vaadin-details-summary>
                <vaadin-vertical-layout theme="spacing-s">
                    <ul>
                        ${this._measure.measures.map(measure => html`<li>${measure}</li>`)}
                    </ul>
                </vaadin-vertical-layout>
            </vaadin-details>`
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

    metadata(metadata, title, emptyMsg) {
        return html`
            <vaadin-details theme="filled">
                <vaadin-details-summary slot="summary">${title}</vaadin-details-summary>
                <vaadin-vertical-layout theme="spacing-s">
                    ${this._metadata(metadata, emptyMsg)}
                </vaadin-vertical-layout>
            </vaadin-details>`
    }

    _metadata(metadata, emptyMsg) {
        if (metadata !== undefined && Object.keys(metadata).length !== 0) {
            return html`<ul>${metadata.map(component => html`<li>${this.component(component)}</li>`)}</ul>`;
        } else {
            return html`${emptyMsg}`;
        }
    }

    component(component) {
        return html`${this.name(component.name)} (index: ${component.index}, unit: ${component.unit}): ${component.description}`;
    }

    name(name) {
        return html`<qui-badge>${name}</qui-badge>`
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
