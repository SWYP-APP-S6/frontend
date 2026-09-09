package com.swyp.mangro.feature.owner.onboarding

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.swyp.mangro.core.designsystem.component.MangroTextField
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

@Composable
internal fun AddressSearchDialog(
    onSearch: suspend (String) -> List<StoreAddress>,
    onDismiss: () -> Unit,
    onSelect: (StoreAddress) -> Unit,
) {
    val query = rememberTextFieldState()
    val scope = rememberCoroutineScope()
    var loading by remember { mutableStateOf(false) }
    var searched by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf(false) }
    var results by remember { mutableStateOf<List<StoreAddress>>(emptyList()) }
    val search = {
        val value = query.text.toString().trim()
        if (value.isNotEmpty() && !loading) {
            loading = true
            results = emptyList()
            error = false
            scope.launch {
                try {
                    results = onSearch(value)
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (_: Exception) {
                    error = true
                } finally {
                    loading = false
                    searched = true
                }
            }
        }
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.address_search)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                MangroTextField(query, stringResource(R.string.search_hint), Modifier.testTag("address-query"), keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search), onKeyboardAction = { search() })
                if (loading) LinearProgressIndicator(Modifier.fillMaxWidth())
                if (error) Text(stringResource(R.string.search_error))
                if (searched && !loading && !error && results.isEmpty()) Text(stringResource(R.string.search_empty))
                LazyColumn(Modifier.heightIn(max = 280.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(results) { address ->
                        Column(Modifier.fillMaxWidth().clickable { onSelect(address) }.padding(vertical = 8.dp)) {
                            Text(address.postalCode)
                            Text(address.address)
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = search, enabled = query.text.isNotBlank() && !loading) { Text(stringResource(R.string.search)) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } },
    )
}
