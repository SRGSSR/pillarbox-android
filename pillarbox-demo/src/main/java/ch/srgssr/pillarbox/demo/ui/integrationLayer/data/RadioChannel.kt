/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.integrationLayer.data

import ch.srg.dataProvider.integrationlayer.request.parameters.Bu

/**
 * Radio channel
 *
 * @property channelId of the channel
 * @property bu of the channel
 * @property label pretty name of the channel
 * @constructor Create empty Radio channel
 */
enum class RadioChannel(val channelId: String, val bu: Bu, val label: String) {
    SRF1("69e8ac16-4327-4af4-b873-fd5cd6e895a7", Bu.SRF, "SRF 1"),
    SRF2Kultur("c8537421-c9c5-4461-9c9c-c15816458b46", Bu.SRF, "SRF 2 Kultur"),
    SRF3("dd0fa1ba-4ff6-4e1a-ab74-d7e49057d96f", Bu.SRF, "SRF 3"),
    SRF4News("ee1fb348-2b6a-4958-9aac-ec6c87e190da", Bu.SRF, "SRF 4 News"),
    SRFMusikwelle("a9c5c070-8899-46c7-ac27-f04f1be902fd", Bu.SRF, "SRF Musikwelle"),
    SRFVirus("66815fe2-9008-4853-80a5-f9caaffdf3a9", Bu.SRF, "SRF Virus"),
    RTSLaPremiere("a9e7621504c6959e35c3ecbe7f6bed0446cdf8da", Bu.RTS, "RTS La 1ère"),
    RTSEspace2("a83f29dee7a5d0d3f9fccdb9c92161b1afb512db", Bu.RTS, "RTS Espace 2"),
    RTSCouleur3("8ceb28d9b3f1dd876d1df1780f908578cbefc3d7", Bu.RTS, "RTS Couleur 3"),
    RTSOptionMusique("f8517e5319a515e013551eea15aa114fa5cfbc3a", Bu.RTS, "RTS Option Musique"),
    RTSPodcastsOriginaux("123456789101112131415161718192021222324x", Bu.RTS, "RTS Podcasts Originaux"),
    RSIReteUno("rete-uno", Bu.RSI, "RSI Rete Uno"),
    RSIReteDue("rete-due", Bu.RSI, "RSI Rete Due"),
    RSIReteTre("rete-tre", Bu.RSI, "RSI Rete Tre"),
    RTR("12fb886e-b7aa-4e55-beb2-45dbc619f3c4", Bu.RTR, "RTR");
}
