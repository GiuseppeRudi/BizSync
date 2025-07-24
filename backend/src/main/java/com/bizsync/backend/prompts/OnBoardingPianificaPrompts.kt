package com.bizsync.backend.prompts



object OnBoardingPianificaPrompts {
    fun getAreaLavoroPrompt(nomeAzienda: String): String {
        return """
        Sei un esperto di organizzazione aziendale.

        COMPITO: Genera esattamente 10 nomi di aree di lavoro tipiche per un'azienda di tipo "$nomeAzienda".

        FORMATO RICHIESTO: Rispondi ESCLUSIVAMENTE con un array JSON di stringhe, senza alcun testo aggiuntivo.

        ESEMPIO DI OUTPUT RICHIESTO:
        [
            "Reception",
            "Vendite",
            "Amministrazione"
        ]

        IMPORTANTE: Rispondi SOLO con il JSON, nient'altro.
    """.trimIndent()
    }

    fun getTurniFrequentiPrompt(nomeAzienda: String): String {
        return """
            Sei un esperto di organizzazione aziendale.
            
            COMPITO: Genera esattamente 6 turni frequenti tipici per un'azienda di tipo "$nomeAzienda".
            
            FORMATO RICHIESTO: Rispondi ESCLUSIVAMENTE con un array JSON valido, senza alcun testo aggiuntivo.
            
            ESEMPIO DI OUTPUT RICHIESTO:
            [
                {"nome": "Mattina", "oraInizio": "08:00", "oraFine": "12:00"},
                {"nome": "Pomeriggio", "oraInizio": "13:00", "oraFine": "17:00"},
            ]
            
            IMPORTANTE: Rispondi SOLO con il JSON, nient'altro.
        """.trimIndent()
    }
}

