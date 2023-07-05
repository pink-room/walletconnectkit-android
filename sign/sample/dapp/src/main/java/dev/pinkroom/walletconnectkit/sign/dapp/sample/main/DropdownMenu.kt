package dev.pinkroom.walletconnectkit.sign.dapp.sample.main

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ExposedDropdownMenuBox
import androidx.compose.material.ExposedDropdownMenuDefaults
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun <T> DropdownMenu(
    modifier: Modifier = Modifier,
    items: List<Pair<String, T>>,
    selectedItem: Pair<String, T>,
    onItemClick: (Pair<String, T>) -> Unit,
    leadingIcon: @Composable (() -> Unit)? = null,
    itemContent: @Composable (Pair<String, T>) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        modifier = modifier,
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
    ) {
        TextField(
            modifier = Modifier.wrapContentHeight().padding(2.dp),
            value = selectedItem.first.middleOverflow(),
            shape = RoundedCornerShape(8.dp),
            onValueChange = {},
            readOnly = true,
            leadingIcon = leadingIcon,
            trailingIcon = {
                if (items.size > 1) ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            colors = TextFieldDefaults.textFieldColors(
                textColor = MaterialTheme.colorScheme.onBackground,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
            )
        )
        if (items.size > 1) {
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
            ) {
                items.forEach { item ->
                    DropdownMenuItem(
                        onClick = {
                            expanded = false
                            onItemClick(item)
                        },
                    ) { itemContent(item) }
                }
            }
        }
    }
}