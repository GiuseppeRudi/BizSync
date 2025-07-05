package com.bizsync.ui.model


import com.bizsync.domain.constants.enumClass.CompanyOperation
import com.bizsync.domain.model.AreaLavoro
import com.bizsync.ui.components.DialogStatusType

data class CompanyState(
    val selectedOperation : CompanyOperation? = null,
    val onBoardingDone: Boolean? = null,

    val resultMsg: String? = null,
    val statusMsg: DialogStatusType = DialogStatusType.ERROR,
    val isLoading: Boolean = false,

    val  areeModificate : List<AreaLavoro> = emptyList(),


    val showAddDialog : Boolean = false,
    val editingArea : AreaLavoro? = null,

    val hasChanges : Boolean = false,

)
