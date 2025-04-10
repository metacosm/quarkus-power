import {html} from 'lit-element';
import '@vaadin/details';
import '@vaadin/vertical-layout';
import 'qui-badge';

class QwcPowerDisplay {
    hilite(msg, isPill) {
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
                html`
                <qui-badge level="${level}" small pill>${msg}</qui-badge>` :
                html`
                <qui-badge level="${level}" small>${msg}</qui-badge>`
        }
    }

    hilitePower(power) {
        if (power) {
            let level;
            if (power < 0.5) {
                level = 'contrast';
            } else if (power < 1) {
                level = 'default';
            } else if (power < 5) {
               level = 'success';
            } else if (power < 10) {
                level = 'warning';
            } else {
                level = 'error';
            }
            return html`<qui-badge level="${level}" primary>${power}</qui-badge>`;
        }
    }

    name(name) {
        return html`<qui-badge>${name}</qui-badge>`
    }

    measures(measures) {
        if (measures) {
            return html`${measures.map(measure => this.measureList(measure.name, measure.measures))}`
        } else {
            return html`No measures`;
        }
    }

    measureList(name, measureList) {
        if (measureList) {
            return html`
            <vaadin-details theme="filled">
                <vaadin-details-summary slot="summary">
                     ${this.name(name)} measures (called ${measureList.length} times)
                </vaadin-details-summary>
                <vaadin-vertical-layout theme="spacing-s">
                    <ol>
                        ${measureList.map(m => html`<li>${this.measure(m)}</li>`)}
                    </ol>
                </vaadin-vertical-layout>
            </vaadin-details>`
        }
    }

    measure(measure) {
        if (measure) {
            return html`${this.hilitePower(measure.power)}mW (${measure.durationMs}ms)`;
        }
    }

    displayMeasure(measure) {
        if (measure) {
            return html`
            <vaadin-details theme="filled">
                <vaadin-details-summary slot="summary">
                    Measure ${measure.summary} (${measure.samplesCount} samples)
                </vaadin-details-summary>
                <vaadin-vertical-layout theme="spacing-s">
                    <ul>
                        ${measure.measures.map(m => html`<li>${this.hilitePower(m)}mW</li>`)}
                    </ul>
                </vaadin-vertical-layout>
            </vaadin-details>`
        }
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

    component(component) {
        return html`${this.name(component.name)} (index: ${component.index}, unit: ${component.unit}): ${component.description}`;
    }

    _metadata(metadata, emptyMsg) {
        if (metadata !== undefined && Object.keys(metadata).length !== 0) {
            return html`<ul>${metadata.map(component => html`<li>${this.component(component)}</li>`)}</ul>`;
        } else {
            return html`${emptyMsg}`;
        }
    }
}

export const display = new QwcPowerDisplay();
