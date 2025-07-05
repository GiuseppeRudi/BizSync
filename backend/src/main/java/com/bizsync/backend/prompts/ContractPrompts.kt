package com.bizsync.backend.prompts

object ContractPrompts {

    fun getCcnlInfoPrompt(
        posizioneLavorativa: String,
        dipartimento: String,
        settoreAziendale: String,
        tipoContratto: String,
        oreSettimanali: String
    ): String {
        return """
            Sei un esperto di diritto del lavoro italiano e CCNL (Contratti Collettivi Nazionali di Lavoro).
            
            COMPITO: Genera le informazioni contrattuali italiane secondo il CCNL appropriato per la seguente posizione lavorativa:
            
            DATI FORNITI:
            - Posizione Lavorativa: "$posizioneLavorativa"
            - Dipartimento: "$dipartimento"
            - Settore Aziendale: "$settoreAziendale"
            - Tipo Contratto: "$tipoContratto"
            - Ore Settimanali: "$oreSettimanali"
            
            FORMATO RICHIESTO: Rispondi ESCLUSIVAMENTE con un JSON valido nel seguente formato, senza alcun testo aggiuntivo:
            
            {
                "settore": "Nome del settore CCNL appropriato",
                "ruolo": "Ruolo/Qualifica secondo CCNL",
                "ferieAnnue": numero_giorni_ferie_annue,
                "rolAnnui": numero_ore_rol_annue,
                "stipendioAnnualeLordo": stipendio_annuale_lordo_euro,
                "malattiaRetribuita": numero_giorni_malattia_retribuita_annui
            }
            
            LINEE GUIDA:
            - Utilizza i CCNL italiani più comuni (Commercio, Metalmeccanici, Edilizia, Pubblici Esercizi, ecc.)
            - Considera il tipo di contratto ($tipoContratto) e le ore settimanali ($oreSettimanali)
            - Fornisci valori realistici basati sui CCNL italiani vigenti
            - Per part-time, proporziona i valori alle ore settimanali
            
            IMPORTANTE: Rispondi SOLO con il JSON, nient'altro.
        """.trimIndent()
    }
}